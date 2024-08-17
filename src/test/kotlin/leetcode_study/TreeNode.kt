package leetcode_study

class TreeNode(var `val`: Int) {
    var left: TreeNode? = null
    var right: TreeNode? = null

    companion object {
        fun of(vararg numbers: Int?): TreeNode? {
            fun setChild(node: TreeNode?, nums: List<Int?>, index: Int): TreeNode? {
                if (node == null) return null
                val (leftIndex, rightIndex) = index * 2 to index * 2 + 1

                if (leftIndex < nums.size && nums[leftIndex] != null) {
                    node.left = TreeNode(nums[leftIndex]!!)
                    setChild(node.left, nums, leftIndex)
                }
                if (rightIndex < nums.size && nums[rightIndex] != null) {
                    node.right = TreeNode(nums[rightIndex]!!)
                    setChild(node.right, nums, rightIndex)
                }
                return node
            }
            val list: List<Int?> = numbers.toMutableList().apply {
                this.add(0, 0)
            }
            return setChild(TreeNode(list[1]!!), list, 1)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TreeNode

        if (`val` != other.`val`) return false
        if (left != other.left) return false
        return right == other.right
    }

    override fun hashCode(): Int {
        var result = `val`
        result = 31 * result + (left?.hashCode() ?: 0)
        result = 31 * result + (right?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "TreeNode(`val`=$`val`, left=$left, right=$right)"
    }
}
