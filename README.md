ENSIME-sbt-cmd is an sbt plugin that enables integration with the ENSIME IDE.

## how to setup
Add the following to your `~/.sbt/plugins/build.sbt`:

    resolvers += "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots"

    addSbtPlugin("org.ensime" % "ensime-sbt-cmd" % "0.0.4-SNAPSHOT")

## how to use
The above automatically adds the `ensime` command to your sbt build. This command will be used automatically by the ENSIME server process to extract info about your project.

## how to make sure it's working
Start sbt for your project. Enter 'ensime dump root' at the prompt. JSON-encoded information about your project should be printed to the console.

## License
BSD License
