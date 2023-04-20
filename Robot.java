import java.util.List;

class Robot {
	int id;
	int startNode;
	List<Integer> path;
	int currentNode;
	int targetNode;

	public Robot(int id, int startNode) {
		this.id = id;
		this.startNode = startNode;
		this.currentNode = startNode;
	}

	public Robot(int id, int startNode, List<Integer> path, int currentNode, int targetNode) {
		super();
		this.id = id;
		this.startNode = startNode;
		this.path = path;
		this.currentNode = currentNode;
		this.targetNode = targetNode;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getStartNode() {
		return startNode;
	}

	public void setStartNode(int startNode) {
		this.startNode = startNode;
	}

	public List<Integer> getPath() {
		return path;
	}

	public void setPath(List<Integer> path) {
		this.path = path;
	}

	public int getCurrentNode() {
		return currentNode;
	}

	public void setCurrentNode(int currentNode) {
		this.currentNode = currentNode;
	}

	public int getTargetNode() {
		return targetNode;
	}

	public void setTargetNode(int targetNode) {
		this.targetNode = targetNode;
	}

	@Override
	public String toString() {
		return "Robot [id=" + id + ", startNode=" + startNode + ", path=" + path + ", currentNode=" + currentNode
				+ ", targetNode=" + targetNode + "]";
	}

}
