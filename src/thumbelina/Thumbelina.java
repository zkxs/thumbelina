package thumbelina;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Hashtable;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

public class Thumbelina
{
	public static void main(String[] args)
	{
		Hashtable<String, Object> options = processArgs(args);
		
		float quality = (Float) options.get("quality");
		String directory = (String) options.get("directory");
		int maxWidth = (Integer) options.get("maxWidth");
		
		System.out.printf("Quality = %f\n", quality);
		System.out.printf("Max Width = %d\n", maxWidth);
		System.out.printf("Directory = \"%s\"\n", directory);
		
		Path path = getPath(directory);
		
		try
		{
			Files.walk(path, 1).forEach(p -> processFile(p, quality, maxWidth));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void processFile(Path p, float quality, int maxWidth)
	{
		if (!Files.isRegularFile(p)) return;
		System.out.printf("Processing: %s\n", p.toString());
		
		try
		{
			BufferedImage inputImage = null;
			try (InputStream in = Files.newInputStream(p))
			{
				inputImage = ImageIO.read(in);
			}
			
			if (inputImage == null)
			{
				// stream could not be read as an image
				return;
			}
			
			String inputFilename = p.getFileName().toString();
			if (inputFilename.endsWith("_thumb.jpg"))
			{
				// already processed
				return;
			}
			
			System.out.printf("Processing: %s for realsies\n", p.toString());
			
			BufferedImage outputImage = scale(inputImage, maxWidth);
			inputImage = null;
			
			// get output path
			int idx = inputFilename.lastIndexOf('.');
			String outputFilename;
			if (idx == -1)
			{
				// there is no '.'
				outputFilename = inputFilename + "_thumb.jpg";
			}
			else
			{
				// '.' is last character
				outputFilename = inputFilename.substring(0, idx) + "_thumb.jpg";
			}
			Path output = p.getParent().resolve(outputFilename);
			
			// setup JPG writer
			ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
			ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
			jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			jpgWriteParam.setCompressionQuality(quality);
			
			//OutputStream outputStream = Files.newOutputStream(output, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			FileImageOutputStream outputStream = new FileImageOutputStream(output.toFile());
			
			jpgWriter.setOutput(outputStream);
			IIOImage outputIIOImage = new IIOImage(outputImage, null, null);
			jpgWriter.write(null, outputIIOImage, jpgWriteParam);
			jpgWriter.dispose();
			outputStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static BufferedImage scale(BufferedImage before, int maxWidth)
	{
		int w = before.getWidth();
		int h = before.getHeight();
		BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		at.scale(2.0, 2.0);
		AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
		after = scaleOp.filter(before, after);
		return after;
	}
	
	public static Path getPath(String directory)
	{
		Path path = Paths.get(directory);
		if (!Files.isDirectory(path)) throw new IllegalArgumentException("Not a directory");
		return path;
	}
	
	public static Hashtable<String, Object> processArgs(String[] args)
	{
		if (args.length < 1)
		{
			// no directory was specified
			showUsage();
		}
		
		Hashtable<String, Object> options = new Hashtable<>();
		options.put("quality", .75f); // quality is a Float
		options.put("maxWidth", 400); // maxWidth is an Integer
		options.put("directory", args[args.length - 1]); // directory is a String
		
		String currentOption = null; // name of current option
		boolean expectingParam = false; // true if the next 
		for (int i = 0; i < args.length - 1; i++)
		{
			String arg = args[i];
			
			if (arg.startsWith("-")) // then arg is probably an option
			{
				if (!expectingParam) // args should be an option
				{
					if (arg.equals("-q"))
					{
						currentOption = "quality";
						expectingParam = true;
					}
					else if (arg.startsWith("--quality="))
					{
						String quality = arg.substring(arg.indexOf('=') + 1);
						options.put("quality", Float.parseFloat(quality));
					}
					else if (arg.equals("-w"))
					{
						currentOption = "maxWidth";
						expectingParam = true;
					}
					else if (arg.startsWith("--width="))
					{
						String quality = arg.substring(arg.indexOf('=') + 1);
						options.put("maxWidth", Integer.parseInt(quality));
					}
					else
					{
						// invalid argument
						showUsage();
					}
				}
				else // args is a param
				{
					try
					{
						switch (currentOption)
						{
							case "quality":
								options.put(currentOption, Float.parseFloat(arg));
								expectingParam = false;
								break;
							case "maxWidth":
							{
								options.put(currentOption, Integer.parseInt(arg));
								expectingParam = false;
								break;
							}
						}
					}
					catch (NumberFormatException e)
					{
						// could not parse
						showUsage();
					}
				}
			}
		}
		
		return options;
	}
	
	public static void showUsage()
	{
		System.out.println(
				  "Usage:"
				+ "    java -jar Thumbelina.jar [OPTIONS] {FOLDER}"
				+ "        Processes all images in FOLDER"
				+ "    OPTIONS:"
				+ "        -q PERCENT, --quality=PERCENT"
				+ "            set the output JPEG quality to PERCENT%. Is 75 by default.");
		System.exit(1);
	}
}
