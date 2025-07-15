import java.util.HashMap;
import java.util.HashSet;

class UnionFind {
    private final HashMap<Integer, Integer> parent = new HashMap<>(); //<kid,father>
    private final HashMap<Integer, Integer> rank = new HashMap<>();

    public UnionFind() {
    }

    public void add(int id) {
        if (!parent.containsKey(id)) {
            parent.put(id, id);
            rank.put(id, 1);
        }
    }

    // 查找根节点（带路径压缩）
    public int find(int id) {
        if (parent.get(id) != id) {
            parent.put(id, find(parent.get(id))); // 路径压缩
        }
        return parent.get(id);
    }

    public void union(int id1, int id2) {
        int root1 = find(id1);
        int root2 = find(id2);
        if (root1 == root2) {
            return;
        }
        // 按秩合并
        if (rank.get(root1) < rank.get(root2)) {
            parent.put(root1, root2);
        } else if (rank.get(root1) > rank.get(root2)) {
            parent.put(root2, root1);
        } else {
            parent.put(root1, root2);
            rank.put(root2, rank.get(root2) + 1); // 树高度+1
        }

    }

    public boolean isConnected(int id1, int id2) {
        return find(id1) == find(id2);
    }

    public void rebuild(HashSet<Integer> connectTo1, int id1, int id2) {
        HashSet<Integer> block = getBlock(id1);     // 待修正的连通块
        for (int id : connectTo1) {
            parent.put(id, id1);
        }
        block.removeAll(connectTo1);
        for (int id : block) {
            parent.put(id, id2);
        }
    }

    private HashSet<Integer> getBlock(int personId) {    // 返回与person连通的所用人的id集合
        HashSet<Integer> block = new HashSet<>();
        int rep = find(personId);
        for (int id : parent.keySet()) {
            if (find(parent.get(id)) == rep) {
                block.add(id);
            }
        }
        return block;
    }

}