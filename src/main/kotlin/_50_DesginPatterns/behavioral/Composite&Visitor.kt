package _50_DesginPatterns.behavioral

fun main(){
    val root = Folder("root").add(
        File("root_file1"), File("root_file2"),
        Folder("folder1").add(File("folder1_file1"), File("folder1_file2")),
        Folder("folder2").add(File("folder2_file1"), File("folder2_file2"))
    )
    root {
        println("${it.name} : ${if(it is Folder) "Folder" else "File"}")
    }
    //removeAll child
    root( object: FileSystem.Visitor {
        var isSelf = false
        override fun invoke(file:FileSystem) {
            if(!isSelf){
                isSelf = true
                return
            }
            file.parent?.remove(file)
        }
    })
}


sealed class FileSystem(fileName:String){
    fun interface Visitor:(FileSystem)->Unit

    @PublishedApi internal var parent:Folder? = null
    var name = fileName
        private set

    fun rename(newName:String){
        parent?.children?.forEach{
            if(it.name == newName) throw IllegalArgumentException("name already exists")
        }
        name = newName
    }

    abstract operator fun invoke(visitor:Visitor)
}

class Folder(name:String):FileSystem(name){
    private var _children: ArrayList<FileSystem>? = null
    val children:List<FileSystem> get() = _children ?: emptyList()

    override fun invoke(visitor:Visitor){
        visitor(this)
        _children?.forEach{ it.invoke(visitor) }
    }
    fun add(vararg child:FileSystem):Folder{
        if(children.isEmpty()) return this
        (_children ?: arrayListOf<FileSystem>().also{ _children = it })
            .addAll(child)

        child.forEach{it.parent = this}
        return this
    }
    fun remove(vararg child:FileSystem){
        if(children.isEmpty()) return
        _children?.removeAll(child.toSet())
    }
}

class File(name:String):FileSystem(name){
    override fun invoke(visitor:Visitor){
        visitor(this)
    }
}
