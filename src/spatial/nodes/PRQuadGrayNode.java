package spatial.nodes;

import spatial.exceptions.UnimplementedMethodException;
import spatial.kdpoint.KDPoint;
import spatial.knnutils.BoundedPriorityQueue;
import spatial.knnutils.NNData;
import spatial.trees.CentroidAccuracyException;
import spatial.trees.PRQuadTree;

import java.util.Collection;

/** <p>A {@link PRQuadGrayNode} is a gray (&quot;mixed&quot;) {@link PRQuadNode}. It
 * maintains the following invariants: </p>
 * <ul>
 *      <li>Its children pointer buffer is non-null and has a length of 4.</li>
 *      <li>If there is at least one black node child, the total number of {@link KDPoint}s stored
 *      by <b>all</b> of the children is greater than the bucketing parameter (because if it is equal to it
 *      or smaller, we can prune the node.</li>
 * </ul>
 *
 * <p><b>YOU ***** MUST ***** IMPLEMENT THIS CLASS!</b></p>
 *
 *  @author --- Haoran Li ---
 */
public class PRQuadGrayNode extends PRQuadNode{


    /* ******************************************************************** */
    /* *************  PLACE ANY  PRIVATE FIELDS AND METHODS HERE: ************ */
    /* ********************************************************************** */
    private int height;
    private int node_counter;
    private PRQuadNode [] successors;

    public void printNode(){
        for(PRQuadNode node : this.successors){
            if (node instanceof PRQuadBlackNode){
                for(KDPoint p : ((PRQuadBlackNode)node).getPoints()){
                    for(int i = 0; i< p.coords.length;i++){
                        System.out.print("BlackNode => " + p.coords[i] + "     ");
                    }
                }
            }else if (node instanceof PRQuadGrayNode){
                System.out.print("GreyNode   ");
            }else{
                System.out.println("WhiteNode   ");
            }
        }
    }
    /* *********************************************************************** */
    /* ***************  IMPLEMENT THE FOLLOWING PUBLIC METHODS:  ************ */
    /* *********************************************************************** */

    /**
     * Creates a {@link PRQuadGrayNode}  with the provided {@link KDPoint} as a centroid;
     * @param centroid A {@link KDPoint} that will act as the centroid of the space spanned by the current
     *                 node.
     * @param k The See {@link PRQuadTree#PRQuadTree(int, int)} for more information on how this parameter works.
     * @param bucketingParam The bucketing parameter fed to this by {@link PRQuadTree}.
     * @see PRQuadTree#PRQuadTree(int, int)
     */
    public PRQuadGrayNode(KDPoint centroid, int k, int bucketingParam){
        super(centroid, k, bucketingParam); // Call to the super class' protected constructor to properly initialize the object! 
        this.height = 1;
        this.successors = new PRQuadNode[4]; // will be limited to 4 children per node.
        node_counter = 0;
    }


    /**
     * <p>Insertion into a {@link PRQuadGrayNode} consists of navigating to the appropriate child
     * and recursively inserting elements into it. If the child is a white node, memory should be allocated for a
     * {@link PRQuadBlackNode} which will contain the provided {@link KDPoint} If it's a {@link PRQuadBlackNode},
     * refer to {@link PRQuadBlackNode#insert(KDPoint, int)} for details on how the insertion is performed. If it's a {@link PRQuadGrayNode},
     * the current method would be called recursively. Polymorphism will allow for the appropriate insert to be called
     * based on the child object's runtime object.</p>
     * @param p A {@link KDPoint} to insert into the subtree rooted at the current {@link PRQuadGrayNode}.
     * @param k The side length of the quadrant spanned by the <b>current</b> {@link PRQuadGrayNode}. It will need to be updated
     *          per recursive call to help guide the input {@link KDPoint}  to the appropriate subtree.
     * @return The subtree rooted at the current node, potentially adjusted after insertion.
     * @see PRQuadBlackNode#insert(KDPoint, int)
     */
    @Override
    public PRQuadNode insert(KDPoint p, int k) {
        int boundary = (int)Math.pow(2, k-1);
        if ((p.coords[1] >= this.centroid.coords[1] && p.coords[1] <= p.coords[1] + boundary)){
            // target is (?, +)
            if (p.coords[0] >= (this.centroid.coords[0] - boundary) && p.coords[0] < this.centroid.coords[0]){
                // target is (-, +)
                if (this.successors[0] == null){
                    // don't have child in this branch
                    int nextSideLength = (int)Math.pow(2,k-2);
                    KDPoint newCentroid = new KDPoint(this.centroid.coords[0] - nextSideLength, this.centroid.coords[1] + nextSideLength);
                    this.successors[0] = new PRQuadBlackNode(newCentroid, k-1, this.bucketingParam, p);
                }else{
                    this.successors[0] = this.successors[0].insert(p, k-1);
                }
                // Height modification
                int NW_height = (this.successors[0] == null) ? -1 : this.successors[0].height();
                int NE_height = (this.successors[1] == null) ? -1 : this.successors[1].height();
                int SW_height = (this.successors[2] == null) ? -1 : this.successors[2].height();
                int SE_height = (this.successors[3] == null) ? -1 : this.successors[3].height();
                this.height = Math.max(NW_height, Math.max(NE_height, Math.max(SW_height, SE_height))) + 1;
                this.node_counter++;
                return this;
            }else if(p.coords[0] >= this.centroid.coords[0] && p.coords[0] <= (this.centroid.coords[0] + boundary)){
                // target is (+, +)
                if (this.successors[1] == null){
                    // don't have child in this branch
                    int nextSideLength = (int)Math.pow(2,k-2);
                    KDPoint newCentroid = new KDPoint(this.centroid.coords[0] + nextSideLength, this.centroid.coords[1] + nextSideLength);
                    this.successors[1] = new PRQuadBlackNode(newCentroid, k-1, this.bucketingParam, p);
                    
                }else{
                    this.successors[1] = this.successors[1].insert(p, k-1);
                }
                int NW_height = (this.successors[0] == null) ? -1 : this.successors[0].height();
                int NE_height = (this.successors[1] == null) ? -1 : this.successors[1].height();
                int SW_height = (this.successors[2] == null) ? -1 : this.successors[2].height();
                int SE_height = (this.successors[3] == null) ? -1 : this.successors[3].height();
                this.height = Math.max(NW_height, Math.max(NE_height, Math.max(SW_height, SE_height))) + 1;
                this.node_counter ++;
                return this;
            }else{
                throw new CentroidAccuracyException("Out of Bound with k = " + this.k);
            }
        }else if (p.coords[1] < this.centroid.coords[1] && p.coords[1] >= this.centroid.coords[1] - boundary){
            // target is (?, -)
            if (p.coords[0] >= (this.centroid.coords[0] - boundary) && p.coords[0] < this.centroid.coords[0]){
                // target is (-, -)
                if (this.successors[2] == null){
                    // don't have child in this branch
                    int nextSideLength = (int)Math.pow(2,k-2);
                    KDPoint newCentroid = new KDPoint(this.centroid.coords[0] - nextSideLength, this.centroid.coords[1] - nextSideLength);
                    this.successors[2] = new PRQuadBlackNode(newCentroid, k-1, this.bucketingParam, p);
                }else{
                    this.successors[2] = this.successors[2].insert(p, k-1);
                }
                // height modification
                int NW_height = (this.successors[0] == null) ? -1 : this.successors[0].height();
                int NE_height = (this.successors[1] == null) ? -1 : this.successors[1].height();
                int SW_height = (this.successors[2] == null) ? -1 : this.successors[2].height();
                int SE_height = (this.successors[3] == null) ? -1 : this.successors[3].height();
                this.height = Math.max(NW_height, Math.max(NE_height, Math.max(SW_height, SE_height))) + 1;
                this.node_counter ++;
                return this;
            }else if(p.coords[0] >= this.centroid.coords[0] && p.coords[0] <= (this.centroid.coords[0] + boundary)){
                // target is (-, +)
                if (this.successors[3] == null){
                    // don't have child in this branch
                    int nextSideLength = (int)Math.pow(2,k-2);
                    KDPoint newCentroid = new KDPoint(this.centroid.coords[0] + nextSideLength, this.centroid.coords[1] - nextSideLength);
                    this.successors[3] = new PRQuadBlackNode(newCentroid, k-1, this.bucketingParam, p);
                }else{
                    this.successors[3] = this.successors[3].insert(p, k-1);
                }
                // height modification
                int NW_height = (this.successors[0] == null) ? -1 : this.successors[0].height();
                int NE_height = (this.successors[1] == null) ? -1 : this.successors[1].height();
                int SW_height = (this.successors[2] == null) ? -1 : this.successors[2].height();
                int SE_height = (this.successors[3] == null) ? -1 : this.successors[3].height();
                this.height = Math.max(NW_height, Math.max(NE_height, Math.max(SW_height, SE_height))) + 1;
                this.node_counter ++;
                return this;
            }else{
                throw new CentroidAccuracyException("Out of Bound with k = " + this.k);
            }
        }else{
            throw new CentroidAccuracyException("Out of Bound with k = " + this.k);
        }
    }

    /**
     * <p>Deleting a {@link KDPoint} from a {@link PRQuadGrayNode} consists of recursing to the appropriate
     * {@link PRQuadBlackNode} child to find the provided {@link KDPoint}. If no such child exists, the search has
     * <b>necessarily failed</b>; <b>no changes should then be made to the subtree rooted at the current node!</b></p>
     *
     * <p>Polymorphism will allow for the recursive call to be made into the appropriate delete method.
     * Importantly, after the recursive deletion call, it needs to be determined if the current {@link PRQuadGrayNode}
     * needs to be collapsed into a {@link PRQuadBlackNode}. This can only happen if it has no gray children, and one of the
     * following two conditions are satisfied:</p>
     *
     * <ol>
     *     <li>The deletion left it with a single black child. Then, there is no reason to further subdivide the quadrant,
     *     and we can replace this with a {@link PRQuadBlackNode} that contains the {@link KDPoint}s that the single
     *     black child contains.</li>
     *     <li>After the deletion, the <b>total</b> number of {@link KDPoint}s contained by <b>all</b> the black children
     *     is <b>equal to or smaller than</b> the bucketing parameter. We can then similarly replace this with a
     *     {@link PRQuadBlackNode} over the {@link KDPoint}s contained by the black children.</li>
     *  </ol>
     *
     * @param p A {@link KDPoint} to delete from the tree rooted at the current node.
     * @return The subtree rooted at the current node, potentially adjusted after deletion.
     */
    @Override
    public PRQuadNode delete(KDPoint p) {
        int currSideLength = (int)Math.pow(2, k-1);
        if (p.coords[1] >= this.centroid.coords[1] && p.coords[1] <= p.coords[1] + currSideLength){
            // target y value is greater than centroid.
            if(p.coords[0] >= (this.centroid.coords[0] - currSideLength) && p.coords[0] < this.centroid.coords[0]){
                // (-, +)
                if(this.successors[0] == null){
                    return this; // white node.
                }
                this.successors[0] = this.successors[0].delete(p);
                this.node_counter --;
                if (this.node_counter <= this.bucketingParam && 
                (this.successors[0] == null || this.successors[0] instanceof PRQuadBlackNode) && 
                (this.successors[1] == null || this.successors[1] instanceof PRQuadBlackNode) &&
                (this.successors[2] == null || this.successors[2] instanceof PRQuadBlackNode) && 
                (this.successors[3] == null || this.successors[3] instanceof PRQuadBlackNode)){ // Can possibly merge.
                    // if all four nodes could be black node or white node, then merge.
                    
                    PRQuadBlackNode newBlackNode = new PRQuadBlackNode(centroid, k, bucketingParam);
                    // addding all the elements into the new black node.
                    for(PRQuadNode node : this.successors){
                        if(node != null){ // not a white node
                            for(KDPoint point : ((PRQuadBlackNode)node).getPoints()){
                                newBlackNode.insert(point, k);
                            }
                        }
                    }
                    if(newBlackNode.count() == 0){
                        return null; // white node.
                    }
                    this.height --;
                    return newBlackNode;
                    
                }else{ // can NOT merge
                    int NW_height = (this.successors[0] == null) ? -1 : this.successors[0].height();
                    int NE_height = (this.successors[1] == null) ? -1 : this.successors[1].height();
                    int SW_height = (this.successors[2] == null) ? -1 : this.successors[2].height();
                    int SE_height = (this.successors[3] == null) ? -1 : this.successors[3].height();
                    this.height = Math.max(NW_height, Math.max(NE_height, Math.max(SW_height, SE_height))) + 1;
                    
                    return this;
                }
            }else if(p.coords[0] >= this.centroid.coords[0] && p.coords[0] <= (this.centroid.coords[0] + currSideLength)){
                // (+, +)
                if(this.successors[1] == null){
                    return this;
                }
                this.successors[1] = this.successors[1].delete(p);// recursively delete.
                this.node_counter --;
                if (this.node_counter <= this.bucketingParam && 
                (this.successors[0] == null || this.successors[0] instanceof PRQuadBlackNode) && 
                (this.successors[1] == null || this.successors[1] instanceof PRQuadBlackNode) &&
                (this.successors[2] == null || this.successors[2] instanceof PRQuadBlackNode) && 
                (this.successors[3] == null || this.successors[3] instanceof PRQuadBlackNode)){ // Can possibly merge.
                    // if all four nodes could be black node or white node, then merge.
                    
                    PRQuadBlackNode newBlackNode = new PRQuadBlackNode(centroid, k, bucketingParam);
                    // addding all the elements into the new black node.
                    for(PRQuadNode node : this.successors){
                        if(node != null){ // not a white node
                            for(KDPoint point : ((PRQuadBlackNode)node).getPoints()){
                                newBlackNode.insert(point, k);
                            }
                        }
                    }
                    if(newBlackNode.count() == 0){
                        return null; // white node.
                    }
                    this.height --;
                    return newBlackNode;
                    
                }else{ // can NOT merge
                    int NW_height = (this.successors[0] == null) ? -1 : this.successors[0].height();
                    int NE_height = (this.successors[1] == null) ? -1 : this.successors[1].height();
                    int SW_height = (this.successors[2] == null) ? -1 : this.successors[2].height();
                    int SE_height = (this.successors[3] == null) ? -1 : this.successors[3].height();
                    this.height = Math.max(NW_height, Math.max(NE_height, Math.max(SW_height, SE_height))) + 1;
                    // this.node_counter++;
                    return this;
                }
            }else{
                // target out of bound.
                throw new CentroidAccuracyException("Out of Bound");
            }
        }else if(p.coords[1] < this.centroid.coords[1] && p.coords[1] >= this.centroid.coords[1] - currSideLength){
            // target y value is lesser than centroid.
            if(p.coords[0] >= (this.centroid.coords[0] - currSideLength) && p.coords[0] < this.centroid.coords[0]){
                // (-, -)
                if (this.successors[2] == null){
                    return this;
                }
                this.successors[2] = this.successors[2].delete(p);// recursively delete.
                this.node_counter --;
                if (this.node_counter <= this.bucketingParam && 
                (this.successors[0] == null || this.successors[0] instanceof PRQuadBlackNode) && 
                (this.successors[1] == null || this.successors[1] instanceof PRQuadBlackNode) &&
                (this.successors[2] == null || this.successors[2] instanceof PRQuadBlackNode) && 
                (this.successors[3] == null || this.successors[3] instanceof PRQuadBlackNode)){ // Can possibly merge.
                    // if all four nodes could be black node or white node, then merge.
                    
                    PRQuadBlackNode newBlackNode = new PRQuadBlackNode(centroid, k, bucketingParam);
                    // addding all the elements into the new black node.
                    for(PRQuadNode node : this.successors){
                        if(node != null){ // not a white node
                            for(KDPoint point : ((PRQuadBlackNode)node).getPoints()){
                                newBlackNode.insert(point, k);
                            }
                        }
                    }
                    if(newBlackNode.count() == 0){
                        return null; // white node.
                    }
                    this.height --;
                    return newBlackNode;
                    
                }else{ // can NOT merge
                    int NW_height = (this.successors[0] == null) ? -1 : this.successors[0].height();
                    int NE_height = (this.successors[1] == null) ? -1 : this.successors[1].height();
                    int SW_height = (this.successors[2] == null) ? -1 : this.successors[2].height();
                    int SE_height = (this.successors[3] == null) ? -1 : this.successors[3].height();
                    this.height = Math.max(NW_height, Math.max(NE_height, Math.max(SW_height, SE_height))) + 1;
                    return this;
                }
            }else if(p.coords[0] >= this.centroid.coords[0] && p.coords[0] <= (this.centroid.coords[0] + currSideLength)){
                // (+, -)
                if(this.successors[3] == null){
                    return this;
                }
                this.successors[3] = this.successors[3].delete(p);// recursively delete.
                this.node_counter --;
                if (this.node_counter <= this.bucketingParam && 
                (this.successors[0] == null || this.successors[0] instanceof PRQuadBlackNode) && 
                (this.successors[1] == null || this.successors[1] instanceof PRQuadBlackNode) &&
                (this.successors[2] == null || this.successors[2] instanceof PRQuadBlackNode) && 
                (this.successors[3] == null || this.successors[3] instanceof PRQuadBlackNode)){ // Can possibly merge.
                    // if all four nodes could be black node or white node, then merge.
                    
                    PRQuadBlackNode newBlackNode = new PRQuadBlackNode(centroid, k, bucketingParam);
                    // addding all the elements into the new black node.
                    for(PRQuadNode node : this.successors){
                        if(node != null){ // not a white node
                            for(KDPoint point : ((PRQuadBlackNode)node).getPoints()){
                                newBlackNode.insert(point, k);
                            }
                        }
                    }
                    if(newBlackNode.count() == 0){
                        return null; // white node.
                    }
                    this.height --;
                    return newBlackNode;
                    
                }else{ // can NOT merge
                    int NW_height = (this.successors[0] == null) ? -1 : this.successors[0].height();
                    int NE_height = (this.successors[1] == null) ? -1 : this.successors[1].height();
                    int SW_height = (this.successors[2] == null) ? -1 : this.successors[2].height();
                    int SE_height = (this.successors[3] == null) ? -1 : this.successors[3].height();
                    this.height = Math.max(NW_height, Math.max(NE_height, Math.max(SW_height, SE_height))) + 1;
                    return this;
                }
            }else{
                // target out of bound.
                throw new CentroidAccuracyException("Out of Bound");
            }
        }else{
            // target out of bound.
            throw new CentroidAccuracyException("Out of Bound");
        }
    }

    @Override
    public boolean search(KDPoint p){
        int currSideLength = (int)Math.pow(2,this.k-1);
        if (p.coords[1] >= this.centroid.coords[1] && p.coords[1] <= p.coords[1] + currSideLength){
            // target y value is greater than centroid.
            if(p.coords[0] >= (this.centroid.coords[0] - currSideLength) && p.coords[0] < this.centroid.coords[0]){
                // (-, +)
                if (this.successors[0] == null){
                    // search fail
                    return false;
                }else{
                    return this.successors[0].search(p);
                }
            }else if (p.coords[0] >= this.centroid.coords[0] && p.coords[0] <= (this.centroid.coords[0] + currSideLength)){
                // (+, +)
                if (this.successors[1] == null){
                    // search fail
                    return false;
                }else{
                    return this.successors[1].search(p);
                }
            }else{
                // out of bound
                return false;
            }
        }else if (p.coords[1] < this.centroid.coords[1] && p.coords[1] >= this.centroid.coords[1] - currSideLength){
            // target y value is lesser than centroid.
            if(p.coords[0] >= (this.centroid.coords[0] - currSideLength) && p.coords[0] < this.centroid.coords[0]){
                // (-, -)
                if (this.successors[2] == null){
                    // search fail
                    return false;
                }else{
                    return this.successors[2].search(p);
                }
            }else if(p.coords[0] >= this.centroid.coords[0] && p.coords[0] <= (this.centroid.coords[0] + currSideLength)){
                // (+, -)
                if (this.successors[3] == null){
                    // search fail
                    return false;
                }else{
                    return this.successors[3].search(p);
                }
            }else{
                // out of bound
                return false;
            }
        }else{
            // out of bound
            return false;
        }
    }

    @Override
    public int height(){
        return this.height;
    }

    @Override
    public int count(){
        return this.node_counter;
    }

    /**
     * Returns the children of the current node in the form of a Z-ordered 1-D array.
     * @return An array of references to the children of {@code this}. The order is Z (Morton), like so:
     * <ol>
     *     <li>0 is NW</li>
     *     <li>1 is NE</li>
     *     <li>2 is SW</li>
     *     <li>3 is SE</li>
     * </ol>
     */
    public PRQuadNode[] getChildren(){
        return this.successors;
    }

    @Override
    public void range(KDPoint anchor, Collection<KDPoint> results, double range) {
        int currSideLength = (int)Math.pow(2,this.k-1);
        if (anchor.coords[1] >= this.centroid.coords[1] && anchor.coords[1] <= anchor.coords[1] + currSideLength){
            // (?, +)
            if (anchor.coords[0] >= (this.centroid.coords[0] - currSideLength) && anchor.coords[0] < this.centroid.coords[0]){
                // (-, +)
                if (this.successors[0] != null){
                    this.successors[0].range(anchor, results, range);
                }
                for(int i = 0; i< this.successors.length; i++){
                    if (i != 0 && this.successors[i] != null && this.successors[i].doesQuadIntersectAnchorRange(anchor, range)){ // not white node
                        this.successors[i].range(anchor, results, range);
                    }
                }
                
            }else if(anchor.coords[0] >= this.centroid.coords[0] && anchor.coords[0] <= (this.centroid.coords[0] + currSideLength)){
                // (+, +)
                if (this.successors[1] != null){
                    this.successors[1].range(anchor, results, range);
                }
                for(int i = 0; i< this.successors.length; i++){
                    if (i != 1 && this.successors[i] != null && this.successors[i].doesQuadIntersectAnchorRange(anchor, range)){ // not white node
                        this.successors[i].range(anchor, results, range);
                    }
                }
            }else{
                throw new CentroidAccuracyException("Out of Bound");
            }
        }else if(anchor.coords[1] < this.centroid.coords[1] && anchor.coords[1] >= this.centroid.coords[1] - currSideLength){
            // (?, -)
            if(anchor.coords[0] >= (this.centroid.coords[0] - currSideLength) && anchor.coords[0] < this.centroid.coords[0]){
                // (-, -)
                if (this.successors[2] != null){
                    this.successors[2].range(anchor, results, range);
                }
                for(int i = 0; i< this.successors.length; i++){
                    if (i != 2 && this.successors[i] != null && this.successors[i].doesQuadIntersectAnchorRange(anchor, range)){ // not white node
                        this.successors[i].range(anchor, results, range);
                    }
                }
            }else if(anchor.coords[0] >= this.centroid.coords[0] && anchor.coords[0] <= (this.centroid.coords[0] + currSideLength)){
                // (+, -)
                if (this.successors[3] != null){
                    this.successors[3].range(anchor, results, range);
                }
                for(int i = 0; i< this.successors.length; i++){
                    if (i != 3 && this.successors[i] != null && this.successors[i].doesQuadIntersectAnchorRange(anchor, range)){ // not white node
                        this.successors[i].range(anchor, results, range);
                    }
                }
            }else{
                throw new CentroidAccuracyException("Out of Bound");
            }
        }else{
            throw new CentroidAccuracyException("Out of Bound");
        }
    }

    @Override
    public NNData<KDPoint> nearestNeighbor(KDPoint anchor, NNData<KDPoint> n)  {
        NNHelper(anchor, n);
        return n;
    }
    /**
     * private helper method for nearest neighbor.
     * @param anchor
     * @param n
     */
    private void NNHelper(KDPoint anchor, NNData<KDPoint> n){
        int currSideLength = (int)Math.pow(2,this.k-1);
        if (anchor.coords[1] >= this.centroid.coords[1] && anchor.coords[1] <= anchor.coords[1] + currSideLength){
            // (?, +)
            if (anchor.coords[0] < this.centroid.coords[0]){
                if(anchor.coords[0] >= (this.centroid.coords[0] - currSideLength)){
                    // (-, +)
                    if (this.successors[0] != null){
                        this.successors[0].nearestNeighbor(anchor, n);
                    }
                    for(int i = 0; i< this.successors.length; i++){
                        if (i != 0 && this.successors[i] != null){ // not white node
                            if(n.getBestDist() == -1 || this.successors[i].doesQuadIntersectAnchorRange(anchor, n.getBestDist())){
                                this.successors[i].nearestNeighbor(anchor, n);
                            }
                        }
                    }
                }else{ 
                    // anchor is out of bound
                    for(int i = 0; i< this.successors.length; i++){
                        if (this.successors[i] != null){ // not white node
                            if(n.getBestDist() == -1 || this.successors[i].doesQuadIntersectAnchorRange(anchor, n.getBestDist())){
                                this.successors[i].nearestNeighbor(anchor, n);
                            }
                            
                        }
                    }
                }
                
            }else if(anchor.coords[0] >= this.centroid.coords[0]){
                if(anchor.coords[0] <= (this.centroid.coords[0] + currSideLength)){
                    // (+, +)
                    if (this.successors[1] != null){
                        this.successors[1].nearestNeighbor(anchor, n);
                    }
                    for(int i = 0; i< this.successors.length; i++){
                        if (i != 1 && this.successors[i] != null){ // not white node
                            if(n.getBestDist() == -1 || this.successors[i].doesQuadIntersectAnchorRange(anchor, n.getBestDist())){
                                this.successors[i].nearestNeighbor(anchor, n);
                            }
                        }
                    }
                }else{
                    // anchor out of bound to the right.
                    for(int i = 0; i< this.successors.length; i++){
                        if (this.successors[i] != null){ // not white node
                            if(n.getBestDist() == -1 || this.successors[i].doesQuadIntersectAnchorRange(anchor, n.getBestDist())){
                                this.successors[i].nearestNeighbor(anchor, n);
                            }
                            
                        }
                    }
                }
                
            }else{
                throw new CentroidAccuracyException("Out of Bound");
            }
        }else if(anchor.coords[1] < this.centroid.coords[1] && anchor.coords[1] >= this.centroid.coords[1] - currSideLength){
            // (?, -)
            if(anchor.coords[0] < this.centroid.coords[0]){
                if(anchor.coords[0] >= (this.centroid.coords[0] - currSideLength)){
                    // (-, -)
                    if (this.successors[2] != null){
                        this.successors[2].nearestNeighbor(anchor, n);
                    }
                    for(int i = 0; i< this.successors.length; i++){
                        if (i != 2 && this.successors[i] != null){ // not white node
                            if(n.getBestDist() == -1 || this.successors[i].doesQuadIntersectAnchorRange(anchor, n.getBestDist())){
                                this.successors[i].nearestNeighbor(anchor, n);
                            }
                            
                        }
                    }
                }else{
                    // anchor out of bound to the left.
                    for(int i = 0; i< this.successors.length; i++){
                        if (this.successors[i] != null){ // not white node
                            if(n.getBestDist() == -1 || this.successors[i].doesQuadIntersectAnchorRange(anchor, n.getBestDist())){
                                this.successors[i].nearestNeighbor(anchor, n);
                            }
                            
                        }
                    }
                }
                
            }else if(anchor.coords[0] >= this.centroid.coords[0]){
                if(anchor.coords[0] <= (this.centroid.coords[0] + currSideLength)){
                    // (+, -)
                    if (this.successors[3] != null){
                        this.successors[3].nearestNeighbor(anchor, n);
                    }  
                    for(int i = 0; i< this.successors.length; i++){
                        if (i != 3 && this.successors[i] != null ){ // not white node
                            if(n.getBestDist() == -1 || this.successors[i].doesQuadIntersectAnchorRange(anchor, n.getBestDist())){
                                this.successors[i].nearestNeighbor(anchor, n);
                            }
                        }
                    }
                }else{
                    // anchor out of bound to the right.
                    for(int i = 0; i< this.successors.length; i++){
                        if (this.successors[i] != null){ // not white node
                            if(n.getBestDist() == -1 || this.successors[i].doesQuadIntersectAnchorRange(anchor, n.getBestDist())){
                                this.successors[i].nearestNeighbor(anchor, n);
                            }
                        }
                    }
                }
                
            }else{
                throw new CentroidAccuracyException("Out of Bound");
            }
        }else{
            throw new CentroidAccuracyException("Out of Bound");
        }
    }

    @Override
    public void kNearestNeighbors(int k, KDPoint anchor, BoundedPriorityQueue<KDPoint> queue) {
        int currSideLength = (int)Math.pow(2,this.k-1);
        if (anchor.coords[1] >= this.centroid.coords[1] && anchor.coords[1] <= anchor.coords[1] + currSideLength){
            // (?, +)
            if (anchor.coords[0] >= (this.centroid.coords[0] - currSideLength) && anchor.coords[0] < this.centroid.coords[0]){
                // (-, +)
                if (this.successors[0] != null){
                    this.successors[0].kNearestNeighbors(k, anchor, queue);
                }
                for(int i = 0; i< this.successors.length; i++){
                    if (i != 0 && this.successors[i] != null && this.successors[i].doesQuadIntersectAnchorRange(anchor, queue.last().euclideanDistance(anchor))){ // not white node
                        this.successors[i].kNearestNeighbors(k, anchor, queue);
                    }
                }
                
            }else if(anchor.coords[0] >= this.centroid.coords[0] && anchor.coords[0] <= (this.centroid.coords[0] + currSideLength)){
                // (+, +)
                if (this.successors[1] != null){
                    this.successors[1].kNearestNeighbors(k, anchor, queue);
                }
                for(int i = 0; i< this.successors.length; i++){
                    if (i != 1 && this.successors[i] != null && this.successors[i].doesQuadIntersectAnchorRange(anchor, queue.last().euclideanDistance(anchor))){ // not white node
                        this.successors[i].kNearestNeighbors(k, anchor, queue);
                    }
                }
            }else{
                throw new CentroidAccuracyException("Out of Bound");
            }
        }else if(anchor.coords[1] < this.centroid.coords[1] && anchor.coords[1] >= this.centroid.coords[1] - currSideLength){
            // (?, -)
            if(anchor.coords[0] >= (this.centroid.coords[0] - currSideLength) && anchor.coords[0] < this.centroid.coords[0]){
                // (-, -)
                if (this.successors[2] != null){
                    this.successors[2].kNearestNeighbors(k, anchor, queue);
                }
                for(int i = 0; i< this.successors.length; i++){
                    if (i != 2 && this.successors[i] != null && this.successors[i].doesQuadIntersectAnchorRange(anchor, queue.last().euclideanDistance(anchor))){ // not white node
                        this.successors[i].kNearestNeighbors(k, anchor, queue);
                    }
                }
            }else if(anchor.coords[0] >= this.centroid.coords[0] && anchor.coords[0] <= (this.centroid.coords[0] + currSideLength)){
                // (+, -)
                if (this.successors[3] != null){
                    this.successors[3].kNearestNeighbors(k, anchor, queue);
                }
                for(int i = 0; i< this.successors.length; i++){
                    if (i != 3 && this.successors[i] != null && this.successors[i].doesQuadIntersectAnchorRange(anchor, queue.last().euclideanDistance(anchor))){ // not white node
                        this.successors[i].kNearestNeighbors(k, anchor, queue);
                    }
                }
            }else{
                throw new CentroidAccuracyException("Out of Bound");
            }
        }else{
            throw new CentroidAccuracyException("Out of Bound");
        }
    }
}

