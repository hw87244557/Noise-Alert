package com.example.administrator.myapplication;

/**
 * Created by WeiHuang on 2/27/2015.
 */
public class AudioWindow {
    int num_over_threshold;
    short threshold;
    int width;
    int num_of_nodes;
    SampleNode top;
    SampleNode bottom;
    SampleNode iterator;

    public AudioWindow (short threshold,int seconds,int sampleRate){
        top=null;
        bottom=null;
        num_of_nodes=0;
        num_over_threshold=0;
        this.threshold=threshold;
        width=seconds*sampleRate;
    }

    public void push(short a,long t){
        SampleNode tempNode=new SampleNode(a,t);
        if(a>threshold)
            num_over_threshold++;
        if(num_of_nodes==0){
            top=bottom=tempNode;
        }else{
            bottom.next=tempNode;
            bottom=tempNode;
        }
        num_of_nodes++;
        if(num_of_nodes>width)
            pop();
    }

    public boolean isEmpty(){
        if(num_of_nodes==0)
            return true;
        else
            return false;
    }

    public short pop(){
        if(!isEmpty()) {
            short temp = top.value;
            top = top.next;
            num_of_nodes--;
            if (temp > threshold)
                num_over_threshold--;
            return temp;
        }else {
            return 0;
        }
    }

    public boolean isFull(){
        if(num_of_nodes==width)
            return true;
        else
            return false;
    }

    public void setIterator(){
        iterator=new SampleNode((short)0,(long)0);
        iterator.next=top;
    }
    public boolean hasNext(){
        if(iterator.next!=null)
            return true;
        else
            return false;
    }
    public short getNext(){
        iterator=iterator.next;
        return iterator.value;
    }

    public long getTimeStamp(){
        return top.timemills;
    }
    private class SampleNode{
        short value;
        long timemills;
        SampleNode next;
        public SampleNode(short v,long t){
            timemills=t;
            value=v;
        }
    }
}
