import java.io.File

println("Testing Sao Luu and Dai Van presence in TuViLogic.kt")
val tuviLogicPath = "app/src/main/java/com/example/tviai/core/TuViLogic.kt"
val lines = File(tuviLogicPath).readLines()

val dvStars = mutableSetOf<String>()
val luuStars = mutableSetOf<String>()

val regexDv = Regex(""""(ĐV\.[^"]+)"""")
val regexLuu = Regex(""""(L\.[^"]+)"""")

for (line in lines) {
    regexDv.findAll(line).forEach { dvStars.add(it.groupValues[1]) }
    regexLuu.findAll(line).forEach { luuStars.add(it.groupValues[1]) }
}

println("Đại Vận Stars (${dvStars.size}):")
dvStars.sorted().forEach { println(" - $it") }

println("\nLưu Niên Stars (${luuStars.size}):")
luuStars.sorted().forEach { println(" - $it") }

