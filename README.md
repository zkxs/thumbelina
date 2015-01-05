thumbelina
==========

Thumbilina is a command line interface (CLI) thumbnail generator that operates on an entire folder of images. Thumbnails are output as JPEG files with configurable quality. Currently, the only downscaling method is to specify the maximum width you wish your thumbnails to have. Output files have "\_thumb" appended to their filename and their extension changed to "jpg". For example, "Image.png" would become "Image\_thumb.jpg". 

### [Download thumbelina.jar](https://github.com/zkxs/thumbelina/raw/master/thumbelina.jar) version 0.1

Details
-------

* Usage information is available [here](https://github.com/zkxs/thumbelina/wiki/Usage)
* Transparency is removed and replaced with white.
* Bicubic interpolation is used in the scaling operation.

Planned features
----------------

* Verbosity option
* Scaling to height
* Option to select background color (for removing transparency)
* Option to select depth of folder traversal

Developers
----------

This project should import in to Eclipse just fine, although you will have to set the Java System Library yourself.
