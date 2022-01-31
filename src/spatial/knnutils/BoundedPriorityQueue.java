package spatial.knnutils;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;



/**
 * <p>{@link BoundedPriorityQueue} is a priority queue whose number of elements
 * is bounded. Insertions are such that if the queue's provided capacity is surpassed,
 * its length is not expanded, but rather the maximum priority element is ejected
 * (which could be the element just attempted to be enqueued).</p>
 *
 * <p><b>YOU ***** MUST ***** IMPLEMENT THIS CLASS!</b></p>
 *
 * @author  <a href = "https://github.com/jasonfillipou/">Jason Filippou</a>
 *
 * @see PriorityQueue
 * @see PriorityQueueNode
 */
public class BoundedPriorityQueue<T> implements PriorityQueue<T>{

	/* *********************************************************************** */
	/* *************  PLACE YOUR PRIVATE FIELDS AND METHODS HERE: ************ */
	/* *********************************************************************** */
	private int size;
	private ArrayList<PriorityQueueNode<T>> queue;
	private int insertOrder;
	public int concurrentChangeCounter = 0;

	public void printQueue(){
		for(PriorityQueueNode<T> node : this.queue){
			System.out.println(node.getData() +" : "+ node.getPriority());
		}
	}
	/* *********************************************************************** */
	/* ***************  IMPLEMENT THE FOLLOWING PUBLIC METHODS:  ************ */
	/* *********************************************************************** */

	/**
	 * Constructor that specifies the size of our queue.
	 * @param size The static size of the {@link BoundedPriorityQueue}. Has to be a positive integer.
	 * @throws IllegalArgumentException if size is not a strictly positive integer.
	 */
	public BoundedPriorityQueue(int size) throws IllegalArgumentException{
		if (size <= 0){
			throw new IllegalArgumentException();
		}else{
			this.size = size;
			this.queue = new ArrayList<>();
			this.insertOrder = 0;
		}
	}

	/**
	 * <p>Enqueueing elements for BoundedPriorityQueues works a little bit differently from general case
	 * PriorityQueues. If the queue is not at capacity, the element is inserted at its
	 * appropriate location in the sequence. On the other hand, if the object is at capacity, the element is
	 * inserted in its appropriate spot in the sequence (if such a spot exists, based on its priority) and
	 * the maximum priority element is ejected from the structure.</p>
	 * 
	 * @param element The element to insert in the queue.
	 * @param priority The priority of the element to insert in the queue.
	 */
	@Override
	public void enqueue(T element, double priority) {
		PriorityQueueNode<T> newNode = new PriorityQueueNode<>(element, priority, this.insertOrder);
		this.insertOrder ++;
		boolean inserted_flag = false;
		for(int i = 0;i < this.queue.size(); i++){
			// target should be inserted into the previous slot. 
			if(this.queue.get(i).getPriority() > priority){
				this.concurrentChangeCounter ++;
				inserted_flag = true;
				this.queue.add(i, newNode);
				break;
			}
		}
		// if the proper position is not in between the existed nodes, then simply append it to the end of the queue.
		if (inserted_flag == false){
			this.concurrentChangeCounter ++;
			if(this.queue.isEmpty()){
				this.queue.add(0,newNode);
			}else{
				this.queue.add(newNode);
			}
		}
		// eject the last one if the size exceed the max bound.
		if (this.queue.size() > this.size){
			this.queue.remove(this.queue.size()-1);
		}
	}

	@Override
	public T dequeue() {
		if(this.queue.size() != 0){
			this.concurrentChangeCounter ++;
			T deleted_data = this.queue.get(0).getData();
			this.queue.remove(0);
			return deleted_data;
		}else{
			return null;
		}
		
	}

	@Override
	public T first() {
		if(this.queue.size() == 0){
			return null;
		}else{
			return this.queue.get(0).getData();
		}
	}
	
	/**
	 * Returns the last element in the queue. Useful for cases where we want to 
	 * compare the priorities of a given quantity with the maximum priority of 
	 * our stored quantities. In a minheap-based implementation of any {@link PriorityQueue},
	 * this operation would scan O(n) nodes and O(nlogn) links. In an array-based implementation,
	 * it takes constant time.
	 * @return The maximum priority element in our queue, or null if the queue is empty.
	 */
	public T last() {
		if (this.queue.size() == 0){
			return null;
		}else{
			return this.queue.get(this.queue.size()-1).getData();
		}
	}

	/**
	 * Inspects whether a given element is in the queue. O(N) complexity.
	 * @param element The element to search for.
	 * @return {@code true} iff {@code element} is in {@code this}, {@code false} otherwise.
	 */
	public boolean contains(T element){
		for(PriorityQueueNode<T> curr : this.queue){
			if (curr.equals(element)){
				return true;
			}
		}
		return false;
	}

	@Override
	public int size() {
		return this.queue.size();
	}

	@Override
	public boolean isEmpty() {
		return this.queue.size() == 0;
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<>(){
			int index = 0;
			int changeCounter = concurrentChangeCounter;
			@Override
			public boolean hasNext() {
				if (changeCounter != concurrentChangeCounter){
					throw new ConcurrentModificationException();
				}else{
					return (index < queue.size());
				}
			}
			@Override
			public T next() {
				if (changeCounter != concurrentChangeCounter){
					throw new ConcurrentModificationException();
				}else{
					index++;
					return queue.get(index-1).getData();
				}
			}

		};
	}
}
