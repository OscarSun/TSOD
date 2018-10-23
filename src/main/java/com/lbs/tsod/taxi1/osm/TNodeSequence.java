package com.lbs.tsod.taxi1.osm;


public class TNodeSequence extends TNode {
    public int sequence_id;

    public TNodeSequence(TNode node, int sequence_id) {
        super(node);
        this.sequence_id = sequence_id;
    }

    public int compareTo(Object obj) {
        TNodeSequence other = (TNodeSequence) obj;
        return this.sequence_id < other.sequence_id ? -1 : (this.sequence_id > other.sequence_id ? 1 : super.compareTo(other));
    }

}
