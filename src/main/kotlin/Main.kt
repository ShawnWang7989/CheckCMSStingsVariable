import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import xmlUnit.StringData
import xmlUnit.XMLResource
import java.io.File

fun main(args: Array<String>) {

    val serializer = Persister()

    while (true) {
        val map = HashMap<String, StringData>()
        val xmlResource1 = getXMLResource(serializer, "Please input strings1.xml path:")
        xmlResource1.entriesList?.forEach { xmlString ->
            val varNumList = findVarList(xmlString.text)
            if (varNumList.isNotEmpty()) {
                map[xmlString.id!!] = StringData(xmlString.text, varNumList)
            }
        }

        val xmlResource2 = getXMLResource(serializer, "Please input strings2.xml path:")
        println()
        var haveInconsistent = false
        xmlResource2.entriesList?.forEach { xmlString ->
            val varNumList = findVarList(xmlString.text)
            val stringData1 = map[xmlString.id!!]
            if (stringData1 != null && !(varNumList.size == stringData1.varNumList.size && varNumList.containsAll(
                    stringData1.varNumList
                ))
            ) {
                haveInconsistent = true
                println("key: ${xmlString.id}")
                println("strings1: ${stringData1.text}")
                println("strings2: ${xmlString.text}")
                println("---------------------------------")
            }
        }
        if (haveInconsistent) {
            println()
        }else{
            println("\nNo Inconsistent string\n")
        }
    }
}

private fun getXMLResource(serializer: Serializer, title: String): XMLResource {
    while (true) {
        println(title)
        val filePath = readLine()
        if (filePath.isNullOrBlank()) {
            println("ERROR! Incorrect file path!")
            continue
        }
        val file = File(filePath)
        if (!file.exists()) {
            println("ERROR! File not existed!")
            continue
        }
        try {
            return serializer.read(XMLResource::class.java, file.inputStream())
        } catch (e: Exception) {
            println("ERROR! Read file failed!")
        }
    }
}

private fun findVarList(s: String?): List<Int> {
    val list = ArrayList<Int>()
    if (s == null)
        return list
    var index = 0
    while (index >= 0) {
        index = s.indexOf("%", index)
        if (index < 0 || index + 1 >= s.length) {
            return list
        }
        // %% just means %. skip it
        if (s[index + 1] == '%') {
            index += 2
            continue
        }
        val result = getVar(s, index + 1)
        result.first?.apply {
            if (!list.contains(this)) {
                list.add(this)
            }
        }
        index = result.second + 1
    }
    return list
}

private fun getVar(s: String, index: Int): Pair<Int?, Int> { // return var number and end index
    for (i in index until s.length) {
        val c = s[i]
        if (c.isDigit()) {
            continue
        }
        //end of num must be '$' and the sub string length must be more than 0
        if (c != '$' || index == i) {
            return Pair(null, i)
        }
        return Pair(s.substring(index, i).toInt(), i)
    }
    return Pair(null, s.length - 1)
}