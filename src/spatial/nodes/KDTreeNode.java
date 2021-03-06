package spatial.nodes;

import spatial.kdpoint.KDPoint;
import spatial.knnutils.BoundedPriorityQueue;
import spatial.knnutils.NNData;

import java.util.Collection;

/**
 * <p>{@link KDTreeNode} is an abstraction over nodes of a KD-Tree. It is used extensively by
 * {@link spatial.trees.KDTree} to implement its functionality.</p>
 *
 * <p><b>YOU ***** MUST ***** IMPLEMENT THIS CLASS!</b></p>
 *
 * @author  ---- Haoran Li -----
 *
 * @see spatial.trees.KDTree
 */
public class KDTreeNode {


    /* *************************************************************************** */
    /* ************* WE PROVIDE THESE FIELDS TO GET YOU STARTED.  **************** */
    /* ************************************************************************** */
    private KDPoint p;
    private int height;
    private KDTreeNode left, right;

    /* *************************************************************************************** */
    /* *************  PLACE ANY OTHER PRIVATE FIELDS AND YOUR PRIVATE METHODS HERE: ************ */
    /* ************************************************************************************* */

    /* *********************************************************************** */
    /* ***************  IMPLEMENT THE FOLLOWING PUBLIC METHODS:  ************ */
    /* *********************************************************************** */


    /**
     * 1-arg constructor. Stores the provided {@link KDPoint} inside the freshly created node.
     * @param p The {@link KDPoint} to store inside this. Just a reminder: {@link KDPoint}s are
     *          <b>mutable!!!</b>.
     */
    public KDTreeNode(KDPoint p){
        this.p = new KDPoint(p);
        this.height = 0;
        this.left = null;
        this.right = null;
    }

    /**
     * <p>Inserts the provided {@link KDPoint} in the tree rooted at this. To select which subtree to recurse to,
     * the KD-Tree acts as a Binary Search Tree on currDim; it will examine the value of the provided {@link KDPoint}
     * at currDim and determine whether it is larger than or equal to the contained {@link KDPoint}'s relevant dimension
     * value. If so, we recurse right, like a regular BST, otherwise left.</p>
     * @param currDim The current dimension to consider
     * @param dims The total number of dimensions that the space considers.
     * @param pIn The {@link KDPoint} to insert into the node.
     * @see #delete(KDPoint, int, int)
     */
    public void insert(KDPoint pIn, int currDim, int dims){
        if (pIn.coords[currDim] >= (this.p.coords[currDim])){
            // traverse to the right.
            if (this.right == null){
                // right child is null.
                this.right = new KDTreeNode(pIn);
                if (this.left == null){
                    this.height ++; // increment the height by 1
                }
            }else{
                this.right.insert(pIn, ((currDim + 1) == dims ? 0 : currDim + 1), dims);
            }
        }else{
             // traverse to the left
            if (this.left == null){
                //left child is null.
                this.left = new KDTreeNode(pIn);
                if (this.right == null){
                    this.height ++; // increment the height by 1
                }
            }else{
                this.left.insert(pIn, ((currDim + 1) == dims ? 0 : currDim + 1), dims);
            }
        }
    }

    /**
     * <p>Deletes the provided {@link KDPoint} from the tree rooted at this. To select which subtree to recurse to,
     * the KD-Tree acts as a Binary Search Tree on currDim; it will examine the value of the provided {@link KDPoint}
     * at currDim and determine whether it is larger than or equal to the contained {@link KDPoint}'s relevant dimension
     * value. If so, we recurse right, like a regular BST, otherwise left. There exist two special cases of deletion,
     * depending on whether we are deleting a {@link KDPoint} from a node who either:</p>
     *
     * <ul>
     *      <li>Has a NON-null subtree as a right child.</li>
     *      <li>Has a NULL subtree as a right child.</li>
     * </ul>
     *
     * <p>You should consult the class slides, your notes, and the textbook about what you need to do in those two
     * special cases.</p>
     * @param currDim The current dimension to consider.
     * @param dims The total number of dimensions that the space considers.
     * @param pIn The {@link KDPoint} to insert into the node.
     * @see #insert(KDPoint, int, int)
     * @return A reference to this after the deletion takes place.
     */
    public KDTreeNode delete(KDPoint pIn, int currDim, int dims){

        if (this.p.equals(pIn)){
            if (this.left == null && this.right == null){
                return null;
            }else if(this.left != null && this.right == null){
                // target node doesn't have right subtree, find the min currDim in left subtree.
                // set left subtree to right subtree of this.
                KDTreeNode min = this.left.findMin(currDim,((currDim + 1 == dims) ? 0 : currDim + 1),dims);
                this.p = new KDPoint(min.p);
                this.right = this.left;
                this.left = null;
                this.right = this.right.delete(new KDPoint(min.p), ((currDim + 1 == dims) ? 0 : currDim + 1), dims);
                return this;
            }else{
                // either target node have right subtree and left subtree, find in-order successor.
                KDTreeNode min = this.right.findMin(currDim, ((currDim + 1 == dims) ? 0 : currDim + 1), dims);
                this.p = new KDPoint(min.p);
                this.right = this.right.delete(new KDPoint(min.p), ((currDim + 1 == dims) ? 0 : currDim + 1), dims);
                return this;
            }
        }else if (pIn.coords[currDim] >= this.p.coords[currDim]){
            this.right = this.right.delete(pIn, (((currDim + 1) == dims) ? 0 : currDim + 1), dims);
            return this;
        }else{
            // go to the left for searching.
            this.left = this.left.delete(pIn, (((currDim + 1) == dims) ? 0 : currDim + 1), dims);
            return this;
        }
    }
    /**
     * private method for finding the minimum that compare with currDim in the left subtree 
     * @param curr
     * @return min node
     */
    private KDTreeNode findMin(int targetDim, int currDim, int dims){
        if (this == null){
            return null;
        }
        if (this.left == null && this.right == null){
            return this;
        }
        if (targetDim == currDim){
            if (this.left == null){ // no more left subtree, current will be the smallest
                return this;
            }else{
                return this.left.findMin(targetDim, (((currDim + 1) == dims) ? 0 : currDim + 1), dims); 
            }
        }
        KDTreeNode lNode = (this.left == null)? null : this.left.findMin(targetDim, (((currDim + 1) == dims)? 0 : currDim + 1), dims);
        KDTreeNode rNode = (this.right == null)? null : this.right.findMin(targetDim, (((currDim + 1) == dims)? 0 : currDim + 1), dims);
        return min3(lNode, rNode, this, targetDim);
    }
    /**
     *  find the minimum of current node and its left child & right child.
     * @param leftMin
     * @param rightMin
     * @param curr
     * @param targetDim
     * @return min of three
     */
    private KDTreeNode min3(KDTreeNode leftMin, KDTreeNode rightMin, KDTreeNode curr, int targetDim){
        if (leftMin != null && rightMin != null && curr != null){
            KDTreeNode childMin = (leftMin.p.coords[targetDim] >= rightMin.p.coords[targetDim]) ? rightMin : leftMin;
            return (childMin.p.coords[targetDim] >= this.p.coords[targetDim]) ? this : childMin;
        }else if (leftMin == null && rightMin != null && curr != null){
            return (rightMin.p.coords[targetDim] >= this.p.coords[targetDim]) ? this : rightMin;
        }else if (leftMin != null && rightMin == null && curr != null){
            return (leftMin.p.coords[targetDim] >= this.p.coords[targetDim]) ? this : leftMin;
        }else if(leftMin != null && rightMin != null && curr == null){
            return (leftMin.p.coords[targetDim] >= rightMin.p.coords[targetDim]) ? rightMin : leftMin;
        }else if (leftMin == null && rightMin == null && curr != null){
            return curr;
        }else if(leftMin != null && rightMin == null && curr == null){
            return leftMin;
        }else{
            return rightMin;
        }
    }

    /**
     * Searches the subtree rooted at the current node for the provided {@link KDPoint}.
     * @param pIn The {@link KDPoint} to search for.
     * @param currDim The current dimension considered.
     * @param dims The total number of dimensions considered.
     * @return true iff pIn was found in the subtree rooted at this, false otherwise.
     */
    public boolean search(KDPoint pIn, int currDim, int dims){
        // stopping case: the target is found.
        if (this.p.equals(pIn)){
            return true;
        }
        if (pIn.coords[currDim] >= this.p.coords[currDim]){
            // go to the right for searching.
            if (this.right == null){
                return false;
            }else{
                return this.right.search(pIn, ((currDim + 1) == dims ? 0 : currDim + 1), dims);
            }
        }else{
            // go to the left for searching.
            if (this.left == null){
                return false;
            }else{
                return this.left.search(pIn, ((currDim + 1) == dims ? 0 : currDim + 1), dims);
            }
        }
    }

    /**
     * <p>Executes a range query in the given {@link KDTreeNode}. Given an &quot;anchor&quot; {@link KDPoint},
     * all {@link KDPoint}s that have a {@link KDPoint#euclideanDistance(KDPoint) euclideanDistance} of <b>at most</b> range
     * <b>INCLUSIVE</b> from the anchor point <b>except</b> for the anchor itself should be inserted into the {@link Collection}
     * that is passed.</p>
     *
     * <p>Remember: range queries behave <em>greedily</em> as we go down (approaching the anchor as &quot;fast&quot;
     * as our currDim allows and <em>prune subtrees</em> that we <b>don't</b> have to visit as we backtrack. Consult
     * all of our resources if you need a reminder of how these should work.</p>
     *
     * @param anchor The centroid of the hypersphere that the range query implicitly creates.
     * @param results A {@link Collection} that accumulates all the {@link }
     * @param currDim The current dimension examined by the {@link KDTreeNode}.
     * @param dims The total number of dimensions of our {@link KDPoint}s.
     * @param range The <b>INCLUSIVE</b> range from the &quot;anchor&quot; {@link KDPoint}, within which all the
     *              {@link KDPoint}s that satisfy our query will fall. The euclideanDistance metric used} is defined by
     *              {@link KDPoint#euclideanDistance(KDPoint)}.
     */
    public void range(KDPoint anchor, Collection<KDPoint> results, double range, int currDim , int dims){
        if (anchor.coords[currDim] >= this.p.coords[currDim]){
            // go to the right for searching.
            if (this.right == null){
                // current node is in the range, then add it to the results list
                if (this.is_InRange(anchor, range) && !anchor.equals(this.p)){
                    results.add(this.p);
                }
                // if there is left subtree, then 
                if (this.left != null){
                    // check if need further search on left side (prune)
                    if (Math.abs(this.p.coords[currDim] - anchor.coords[currDim]) <= range){
                        // Can NOT prune, check the left side
                        this.left.range(anchor, results, range, ((currDim + 1) == dims ? 0 : currDim + 1), dims);
                    }
                }  
            }else{
                // not reaching the greedy point
                this.right.range(anchor, results, range, ((currDim + 1) == dims ? 0 : currDim + 1), dims);
                // current node is in the range, then add it to the results list
                if (this.is_InRange(anchor, range) && !anchor.equals(this.p)){
                    results.add(this.p);
                }
                // if there is left subtree, then 
                if (this.left != null){
                    // check if need further search on left side (prune)
                    if (Math.abs(this.p.coords[currDim] - anchor.coords[currDim]) <= range){
                        // Can NOT prune, check the left side
                        this.left.range(anchor, results, range, ((currDim + 1) == dims ? 0 : currDim + 1), dims);
                    }
                }
            }
        }else{
            // go to the left for searching.
            if (this.left == null){
                // current node is in the range, then add it to the results list
                if (this.is_InRange(anchor, range) && !anchor.equals(this.p)){
                    results.add(this.p);
                }
                // if there is right subtree, then 
                if (this.right != null){
                    // check if need further search on right side (prune)
                    if (Math.abs(this.p.coords[currDim] - anchor.coords[currDim]) <= range){
                        // Can NOT prune, check the right side
                        this.right.range(anchor, results, range, ((currDim + 1) == dims ? 0 : currDim + 1), dims);
                    }
                }
            }else{
                // not reaching the greedy point.
                this.left.range(anchor, results, range, ((currDim + 1) == dims ? 0 : currDim + 1), dims);
                // current node is in the range, then add it to the results list
                if (this.is_InRange(anchor, range) && !anchor.equals(this.p)){
                    results.add(this.p);
                }
                // if there is right subtree, then 
                if (this.right != null){
                    if (Math.abs(this.p.coords[currDim] - anchor.coords[currDim]) <= range){
                        // Can NOT prune, check the right side
                        this.right.range(anchor, results, range, ((currDim + 1) == dims ? 0 : currDim + 1), dims);
                    }
                }
            }
        }
    }

    private boolean is_InRange(KDPoint anchor, double range){
        return (this.p.euclideanDistance(anchor) <= range);
    }


    /**
     * <p>Executes a nearest neighbor query, which returns the nearest neighbor, in terms of
     * {@link KDPoint#euclideanDistance(KDPoint)}, from the &quot;anchor&quot; point.</p>
     *
     * <p>Recall that, in the descending phase, a NN query behaves <em>greedily</em>, approaching our
     * &quot;anchor&quot; point as fast as currDim allows. While doing so, it implicitly
     * <b>bounds</b> the acceptable solutions under the current <b>best solution</b>, which is passed as
     * an argument. This approach is known in Computer Science as &quot;branch-and-bound&quot; and it helps us solve an
     * otherwise exponential complexity problem (nearest neighbors) efficiently. Remember that when we want to determine
     * if we need to recurse to a different subtree, it is <b>necessary</b> to compare the euclideanDistance reported by
     * {@link KDPoint#euclideanDistance(KDPoint)} and coordinate differences! Those are comparable with each other because they
     * are the same data type ({@link Double}).</p>
     *
     * @return An object of type {@link NNData}, which exposes the pair (distance_of_NN_from_anchor, NN),
     * where NN is the nearest {@link KDPoint} to the anchor {@link KDPoint} that we found.
     *
     * @param anchor The &quot;ancor&quot; {@link KDPoint}of the nearest neighbor query.
     * @param currDim The current dimension considered.
     * @param dims The total number of dimensions considered.
     * @param n An object of type {@link NNData}, which will define a nearest neighbor as a pair (distance_of_NN_from_anchor, NN),
     *      * where NN is the nearest neighbor found.
     *
     * @see NNData
     * @see #kNearestNeighbors(int, KDPoint, BoundedPriorityQueue, int, int)
     */
    public NNData<KDPoint> nearestNeighbor(KDPoint anchor, int currDim, NNData<KDPoint> n, int dims){
        // n = new NNData<KDPoint>(this.p, this.p.euclideanDistance(anchor));
        NNHelper(anchor, currDim, n, dims);
        return n;
    }
    /**
     * Nearest Neighbor helper method
     * @param anchor
     * @param currDim
     * @param n
     * @param dims
     */
    private void NNHelper(KDPoint anchor, int currDim, NNData<KDPoint> n, int dims){
        if (anchor.coords[currDim] >= this.p.coords[currDim]){
            if (this.right == null){
                double currDistance = this.p.euclideanDistance(anchor);
                // current node has shorter distance, then add it to the results list
                if ((n.getBestDist() == -1 ||currDistance <= n.getBestDist()) && !anchor.equals(this.p)){
                    n.update(this.p, currDistance);
                }
                // if there is left subtree, then 
                if (this.left != null){
                    // check if need further search on left side (prune)
                    if (this.left.p.euclideanDistance(anchor) <= n.getBestDist()){
                        // Can NOT prune, check the left side
                        this.left.NNHelper(anchor, ((currDim + 1) == dims ? 0 : currDim + 1), n, dims);
                    }
                }
            }else{
                // if current is the shortest so far, set it to the best
                double currDistance = this.p.euclideanDistance(anchor);
                if ((n.getBestDist() == -1 ||currDistance <= n.getBestDist()) && !anchor.equals(this.p)){
                    n.update(this.p, currDistance);
                }
                // trverse to the right
                this.right.NNHelper(anchor,((currDim + 1) == dims ? 0 : currDim + 1), n, dims);
                // if there is left subtree, then 
                if (this.left != null){
                    // check if need further search on left side (prune)
                    if (this.left.p.euclideanDistance(anchor) <= n.getBestDist()){
                        // Can NOT prune, check the left side
                        this.left.NNHelper(anchor, ((currDim + 1) == dims ? 0 : currDim + 1), n, dims);
                    }
                }
            }
        }else{
            if (this.left == null){
                double currDistance = this.p.euclideanDistance(anchor);
                if ((n.getBestDist() == -1 ||currDistance <= n.getBestDist()) && !anchor.equals(this.p)){
                    n.update(this.p, currDistance);
                }
                // if there is right subtree, then 
                if (this.right != null){
                    // check if need further search on right side (prune)
                    if (this.right.p.euclideanDistance(anchor) <= n.getBestDist()){
                        // Can NOT prune, check the right side
                        this.right.NNHelper(anchor, ((currDim + 1) == dims ? 0 : currDim + 1), n, dims);
                    }
                }
            }else{
                // if current is the shortest so far, set it to the best
                double currDistance = this.p.euclideanDistance(anchor);
                if ((n.getBestDist() == -1 ||currDistance <= n.getBestDist()) && !anchor.equals(this.p)){
                    n.update(this.p, currDistance);
                }
                // traverse to the left.
                this.left.NNHelper(anchor,((currDim + 1) == dims ? 0 : currDim + 1), n, dims); 
                // if there is right subtree, then 
                if (this.right != null){
                    // check if need further search on right side (prune)
                    if (this.right.p.euclideanDistance(anchor) <= n.getBestDist()){
                        // Can NOT prune, check the right side
                        this.right.NNHelper(anchor, ((currDim + 1) == dims ? 0 : currDim + 1), n, dims);
                    }
                }
            }
        }
    }
    
    /**
     * <p>Executes a nearest neighbor query, which returns the nearest neighbor, in terms of
     * {@link KDPoint#euclideanDistance(KDPoint)}, from the &quot;anchor&quot; point.</p>
     *
     * <p>Recall that, in the descending phase, a NN query behaves <em>greedily</em>, approaching our
     * &quot;anchor&quot; point as fast as currDim allows. While doing so, it implicitly
     * <b>bounds</b> the acceptable solutions under the current <b>worst solution</b>, which is maintained as the
     * last element of the provided {@link BoundedPriorityQueue}. This is another instance of &quot;branch-and-bound&quot;
     * Remember that when we want to determine if we need to recurse to a different subtree, it is <b>necessary</b>
     * to compare the euclideanDistance reported by* {@link KDPoint#euclideanDistance(KDPoint)} and coordinate differences!
     * Those are comparable with each other because they are the same data type ({@link Double}).</p>
     *
     * <p>The main difference of the implementation of this method and the implementation of
     * {@link #nearestNeighbor(KDPoint, int, NNData, int)} is the necessity of using the class
     * {@link BoundedPriorityQueue} effectively. Consult your various resources
     * to understand how you should be using this class.</p>
     *
     * @param k The total number of neighbors to retrieve. It is better if this quantity is an odd number, to
     *          avoid ties in Binary Classification tasks.
     * @param anchor The &quot;anchor&quot; {@link KDPoint} of the nearest neighbor query.
     * @param currDim The current dimension considered.
     * @param dims The total number of dimensions considered.
     * @param queue A {@link BoundedPriorityQueue} that will maintain at most k nearest neighbors of
     *              the anchor point at all times, sorted by euclideanDistance to the point.
     *
     * @see BoundedPriorityQueue
     */
    public void kNearestNeighbors(int k, KDPoint anchor, BoundedPriorityQueue<KDPoint> queue, int currDim, int dims){
        if (anchor.coords[currDim] >= this.p.coords[currDim]){// search to the right
            if (this.right == null){
                // reach the greedy point, check the greedy point.
                if(!this.p.equals(anchor)){
                    // if greedy point is NOT anchor.
                    queue.enqueue(this.p, this.p.euclideanDistance(anchor));
                }

                // check if pruning is needed for the left side of current.
                if (this.left != null){
                    // Go to the left side.
                    if (queue.size() != k){
                        // queue is NOT full, then go to the left side to fill in all the space in queue.
                        this.left.kNearestNeighbors(k, anchor, queue, ((currDim + 1) == dims ? 0 : currDim + 1), dims);
                    }else{
                        if(queue.last().euclideanDistance(anchor) >= this.left.p.euclideanDistance(anchor)){
                            // queue is full, but left child have shorter or equal distance than the last in the queue.
                            this.left.kNearestNeighbors(k, anchor, queue, ((currDim + 1) == dims ? 0 : currDim + 1), dims);
                        }
                    }
                }
            }else{
                // if current is NOT anchor, add current to comapre with those that already in the queue.
                if(!this.p.equals(anchor)){
                    queue.enqueue(this.p, this.p.euclideanDistance(anchor));
                }
                // not reaching the greedy point
                this.right.kNearestNeighbors(k, anchor, queue, ((currDim + 1) == dims ? 0 : currDim + 1), dims);
                
                // check if pruning is needed for the left side of current.(when backtracking)
                if (this.left != null){
                    // Go to the left side.
                    if (queue.size() != k){
                        // queue is NOT full, then go to the left side to check.
                        this.left.kNearestNeighbors(k, anchor, queue, ((currDim + 1) == dims ? 0 : currDim + 1), dims);
                    }else{
                        if(queue.last().euclideanDistance(anchor) >= this.left.p.euclideanDistance(anchor)){
                            // queue is full, but left child have shorter or equal distance than the last in the queue.
                            this.left.kNearestNeighbors(k, anchor, queue, ((currDim + 1) == dims ? 0 : currDim + 1), dims);
                        }
                    }
                }
            }
        }else{// search to the left
            if (this.left == null){
                // reach the greedy point, check the greedy point.
                if(!this.p.equals(anchor)){
                    // if greedy point is not anchor.
                    queue.enqueue(this.p, this.p.euclideanDistance(anchor));
                }
                // if right side have subtree , check if pruning is needed.
                if (this.right != null){
                    // Go to the right side.
                    if (queue.size() != k){
                        // queue is NOT full, then go to the right side to check.
                        this.right.kNearestNeighbors(k, anchor, queue, ((currDim + 1) == dims ? 0 : currDim + 1), dims);
                    }else{
                        if(queue.last().euclideanDistance(anchor) >= this.right.p.euclideanDistance(anchor)){
                            // queue is full, but right child have shorter or equal distance than the last in the queue.
                            this.right.kNearestNeighbors(k, anchor, queue, ((currDim + 1) == dims ? 0 : currDim + 1), dims);
                        }
                    }
                }
            }else{
                // add current to comapre with those that already in the queue.
                if(!this.p.equals(anchor)){
                    queue.enqueue(this.p, this.p.euclideanDistance(anchor));
                }
            
                // not reaching the greedy point, keep traversing.
                this.left.kNearestNeighbors(k, anchor, queue, ((currDim + 1) == dims ? 0 : currDim + 1), dims);

                // if right side have subtree , check if pruning is needed.(when backtracking)
                if (this.right != null){
                    // Go to the right side.
                    if (queue.size() != k){
                        // queue is NOT full, then go to the right side to check.
                        this.right.kNearestNeighbors(k, anchor, queue, ((currDim + 1) == dims ? 0 : currDim + 1), dims);
                    }else{
                        if(queue.last().euclideanDistance(anchor) >= this.right.p.euclideanDistance(anchor)){
                            // queue is full, but right child have shorter or equal distance than the last in the queue.
                            this.right.kNearestNeighbors(k, anchor, queue, ((currDim + 1) == dims ? 0 : currDim + 1), dims);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the height of the subtree rooted at the current node. Recall our definition of height for binary trees:
     * <ol>
     *     <li>A null tree has a height of -1.</li>
     *     <li>A non-null tree has a height equal to max(height(left_subtree), height(right_subtree))+1</li>
     * </ol>
     * @return the height of the subtree rooted at the current node.
     */
    public int height(){
        return heightHelper(this);
    }

    private int heightHelper(KDTreeNode curr){
        if (curr == null){
            return -1;
        }else{
            if (curr.left == null && curr.right == null){
                return 0;
            }else{
                return Math.max(heightHelper(curr.left), heightHelper(curr.right)) + 1;
            }
        }
    }

    /**
     * A simple getter for the {@link KDPoint} held by the current node. Remember: {@link KDPoint}s ARE
     * MUTABLE, SO WE NEED TO DO DEEP COPIES!!!
     * @return The {@link KDPoint} held inside this.
     */
    public KDPoint getPoint(){
        return new KDPoint(this.p);
    }

    public KDTreeNode getLeft(){
        return this.left;
    }

    public KDTreeNode getRight(){
        return this.right;
    }
}
