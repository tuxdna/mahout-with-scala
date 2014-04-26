scala-talk
==========

Introduction to Scala Programming Language

To create presentation first install landslide via pip:

    $ sudo yum install -y python-pip
    $ pip-python install landslide

Create the presentation:

    $ landslide mahout-scala-talk.md --relative --copy-theme -i

Open it in your favorite browser:

    $ firefox presentation.html

Update the presentation while you are still working on it:

Install inotify-tools on Ubuntu:

    $ sudo aptitude install inotify-tools

Use inotifywait to invoke rebuild on every change

    $ while inotifywait -e close_write mahout-scala-talk.md ; do landslide mahout-scala-talk.md --relative --copy-theme -i; done
    
