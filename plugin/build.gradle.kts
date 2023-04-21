tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
    options.fork()
    options.encoding = "UTF-8"
}

dependencies {
    api(project(":shared"))
    kapt("gg.scala.commons:bukkit:3.1.9")
    compileOnly("gg.scala.cloudsync:spigot:1.0.1")
    compileOnly("gg.scala.cgs:game:1.1.9")
}
