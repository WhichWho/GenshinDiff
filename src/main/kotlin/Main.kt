import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.io.path.createLinkPointingTo

const val PKG_VERSION = "pkg_version"

fun main(args: Array<String>) {
    if (args.size == 3) {
        makeDiffPackage(args[0], args[1], args[2])
    } else if (args.size == 4) {
        when (args[0]) {
            "-link" -> makeRetainLinks(args[1], args[2], args[3])
            "-bat" -> makeHardlinkBat(args[1], args[2], args[3])
            else -> printHelp()
        }
    } else {
        printHelp()
    }
}

private fun printHelp() {
    println(
        """
            use:
            generate diff package to output_folder
                java -jar GenshinDiff.jar old_version_folder new_version_folder output_folder
            make hard link base on old_version_folder
                java -jar GenshinDiff.jar -link old_version_folder new_version_folder output_folder
            make hard link bat
                java -jar GenshinDiff.jar -bat old_version_folder new_version_folder output.bat
        """.trimIndent()
    )
}

fun makeHardlinkBat(src: String, dst: String, bat: String) {
    val srcPkgFile = File(src, PKG_VERSION)
    val dstPkgFile = File(dst, PKG_VERSION)
    if (!srcPkgFile.exists() || !dstPkgFile.exists()) return
    File(bat).outputStream().bufferedWriter().use { w ->
        w.appendLine(
            """
        set src=$src
        set dst=$dst
        """.trimIndent()
        )
        w.newLine()
        val dirs = mutableSetOf<String>()
        val files = mutableListOf<String>()
        val srcFiles = srcPkgFile.readInfo()
        dstPkgFile.readInfo().forEach { info ->
            val indexOf = srcFiles.indexOf(info)
            if (indexOf == -1) return@forEach
            val srcRemoteName = srcFiles[indexOf].remoteName
            val folder = info.remoteName.substringBeforeLast("/", "")
            if (folder != "") {
                dirs += "mkdir \"%dst%${folder.flip()}\""
            }
            files += "mklink /H \"%dst%${info.remoteName.flip()}\" \"%src%${srcRemoteName.flip()}\""
        }
        dirs.forEach { w.appendLine(it) }
        files.forEach { w.appendLine(it) }
    }
}

fun String.flip() = replace("/", "\\")

fun makeRetainLinks(src: String, dst: String, diff: String) {
    makeRetainLinks(src, File(dst, PKG_VERSION), diff)
}

// old folder,
// new pkg_version File,
// diff folder
fun makeRetainLinks(src: String, dstPkgFile: File, diff: String) {
    val srcPkgFile = File(src, PKG_VERSION)
    if (!srcPkgFile.exists() || !dstPkgFile.exists()) return

//    val retainFiles = dstPkgFile.readInfo().toMutableList().also {
//        it.retainAll(srcPkgFile.readInfo())
//    }
//    retainFiles.forEach { info ->
//        val target = File(diff, info.remoteName)
//        target.parentFile.let {
//            if (!it.exists()) it.mkdirs()
//        }
//        target.toPath().createLinkPointingTo(
//            File(src, info.remoteName.os2cn()).toPath()
//        )
//    }

    // cn -> os, os -> os, cn -> cn, os -> cn
    // ctmd.
    val srcFiles = srcPkgFile.readInfo()
    dstPkgFile.readInfo().forEach { info ->
        val indexOf = srcFiles.indexOf(info)
        if (indexOf == -1) return@forEach
        val target = File(diff, info.remoteName)
        target.parentFile.let {
            if (!it.exists()) it.mkdirs()
        }
        target.toPath().createLinkPointingTo(
            File(src, srcFiles[indexOf].remoteName).toPath()
        )
    }
}

fun makeDiffPackage(src: String, dst: String, diff: String) {
    val srcPkgFile = File(src, PKG_VERSION)
    val dstPkgFile = File(dst, PKG_VERSION)
    if (!srcPkgFile.exists() || !dstPkgFile.exists()) return
    val diffFiles = dstPkgFile.readInfo() - srcPkgFile.readInfo()
    println("pkg size: %.2fGB".format(diffFiles.sumOf { it.fileSize } / 1024f / 1024f / 1024f))
    println("file count: ${diffFiles.size}")
    diffFiles.forEach { info ->
        val target = File(diff, info.remoteName)
        target.parentFile.let {
            if (!it.exists()) it.mkdirs()
        }
        File(dst, info.remoteName).copyTo(target)
    }
    dstPkgFile.copyTo(File(diff, PKG_VERSION))
}

fun File.readInfo() = this.reader().useLines { sequence ->
//    sequence.map { it.fromJson<FileInfo>() }.toSet()
    sequence.map { it.fromJson<FileInfo>() }.toList()
}

inline fun <reified T> T.toJson() = Json.encodeToString(this)

inline fun <reified T> String.fromJson() = Json.decodeFromString<T>(this)
