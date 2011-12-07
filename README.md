XXX Work in progress -- Below instructions probably don't work XXX
------
ENSIME-sbt-cmd is an sbt plugin that enables integration with the ENSIME IDE.

## how to setup
Add the following to your `~/.sbt/plugins/build.sbt`:

    addSbtPlugin("org.ensime" % "ensime-sbt-cmd" % "0.0.1")

Then, for some reason with sbt 0.11.1, you have to do the following from sbt in your project:

    > reload plugins
    > clean
    > reload return

## how to use
The above automatically adds the `ensime-dump` command. This command will be used by the ENSIME server process to glean info about your project.

## License
MIT License.
