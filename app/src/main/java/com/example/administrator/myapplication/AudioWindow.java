package com.example.administrator.myapplication;

/**
 * Created by WeiHuang on 2/27/2015.
 */
public class AudioWindow {
    int num_over_threshold;
    short threshold;
    int width;
    int num_of_nodes;
    long cumulate;
    SampleNode top;
    SampleNode bottom;
    SampleNode iterator;

    public AudioWindow (short threshold,int seconds,int sampleRate){
        top=null;
        bottom=null;
        num_of_nodes=0;
        num_over_threshold=0;
        cumulate=0;
        this.threshold=threshold;
        width=seconds*sampleRate;
    }

    public void push(short a,long t){
        SampleNode tempNode=new SampleNode(a,t);
        if(Math.abs(a)>threshold)
            num_over_threshold++;
        cumulate=cumulate+Math.abs(a);
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

    public void pop(){
        if(!isEmpty()) {
            short temp = top.value;
            top = top.next;
            num_of_nodes--;
            cumulate=cumulate-Math.abs(temp);
            if (Math.abs(temp) > threshold){
                num_over_threshold--;
            }
        }
    }

    public boolean isFull(){
        if(num_of_nodes==width){
            return true;
        }
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
    public String[] getNext(){
        iterator=iterator.next;
        String[] strs={Short.toString(iterator.value),Long.toString(iterator.timeMills)};
        return strs;
    }

    public long getTimeStamp(){
        return top.timeMills;
    }
    public int getNumOverThreshold(){ return num_over_threshold;}
    public int getAverageAmplitude(){ return (int)(cumulate/((long)num_of_nodes));}
    private class SampleNode{
        short value;
        long timeMills;
        SampleNode next;
        public SampleNode(short v,long t){
            timeMills=t;
            value=v;
        }
    }
}
