ENSIME-sbt-cmd is an sbt plugin that supports integration with the ENSIME IDE.

## how to setup
Add the following to your `~/.sbt/plugins/build.sbt`:

    resolvers += "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots"

    addSbtPlugin("org.ensime" % "ensime-sbt-cmd" % "0.0.7-SNAPSHOT")

## how to use
The above automatically adds the `ensime generate` command to your sbt build. This command will write a .ensime file to your project's root directory.

## License
BSD License
