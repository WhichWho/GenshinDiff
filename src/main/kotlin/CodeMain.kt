
fun main(args: Array<String>) {
    val src = "D:\\Program Files\\Genshin Impact\\Genshin Impact Game"
    val dst = "C:\\Users\\hp\\Desktop\\GenshinImpact_3.0.51.1_beta"
    val diff = "D:\\Download\\GenshinImpactBeta"
    makeRetainLinks(src, dst, diff)
    makeDiffPackage(src, dst, diff)
}