package com.renju_note.isoo;

import android.content.Context;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SeqTree implements Serializable {
    private Node head = new Node(-1,-1);
    private Node now;
    private int[][] now_board = new int[15][15];
    private String text_box;

    public class Node implements Serializable{
        private int x,y;
        private Node chlid;
        private Node next;
        private Node parent;

        private String text;
        //private String text_box;

        public Node(int x, int y) {
            this.x = x;
            this.y = y;
            this.chlid = null;
            this.next = null;
            this.parent = null;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public Node getChlid() {
            return chlid;
        }

        public Node getNext() { return  next; }

        public Node getParent() {
            return parent;
        }

        public void setChlid(Node chlid) {
            this.chlid = chlid;
        }

        public void setNext(Node next) {
            this.next = next;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public void next(Node current_node,int x,int y) {
        if(current_node == null || current_node.getChlid() == null) {
            Node newChild = new Node(x,y);
            if (head.getChlid() == null) {
                head.chlid = newChild;
                newChild.parent = head;
                current_node = head;
            }
            newChild.parent = current_node;
            current_node.chlid = newChild;
            now = newChild;
            now_board[x][y] = 1;
        } else {
            for(Node temp = current_node.chlid;temp != null;temp = temp.next) {
                if(temp.x == x && temp.y == y) {
                    now = temp;
                    now_board[x][y] = 1;
                    return;
                }
                if(temp.next == null) {
                    Node newNext = new Node(x,y);
                    newNext.parent = current_node;
                    temp.next = newNext;
                    now = newNext;
                    now_board[x][y] = 1;
                    return;
                }
            }
        }
    }

    public void undo(Node current_node) {
        now = current_node.parent;
        now_board[current_node.x][current_node.y] = 0;
    }

    public void redo(Node current_node) {
        if(current_node.chlid != null && current_node.chlid.next == null) {
            now = current_node.getChlid();
            now_board[current_node.getChlid().getX()][current_node.getChlid().getY()] = 1;
        }
    }

    public void delete(Node current_node) {
        now = current_node.parent;
        if(now.chlid.next == null) {
            now.setChlid(null);
        } else {
            if(now.getChlid().getX() == current_node.getX() && now.getChlid().getY() == current_node.getY()) {
                now.setChlid(now.getChlid().getNext());
            } else {
                for (Node temp = now.chlid; ; temp = temp.next) {
                    if (temp.getNext().getX() == current_node.getX() && temp.getNext().getY() == current_node.getY()) {
                        if (temp.getNext().getNext() == null) {
                            temp.setNext(null);
                        } else {
                            temp.setNext(temp.getNext().getNext());
                        }
                        break;
                    }
                }
            }
        }
        now_board[current_node.x][current_node.y] = 0;
    }

    public void createChild(Node current_node, int x, int y) {
        if(current_node == null || current_node.getChlid() == null) {
            Node newChild = new Node(x,y);
            if (head.getChlid() == null) {
                head.chlid = newChild;
                newChild.parent = head;
                current_node = head;
            }
            newChild.parent = current_node;
            current_node.chlid = newChild;
            //now = newChild;
            //now_board[x][y] = 1;
        } else {
            for(Node temp = current_node.chlid;temp != null;temp = temp.next) {
                if(temp.x == x && temp.y == y) {
                    //now = temp;
                    //now_board[x][y] = 1;
                    return;
                }
                if(temp.next == null) {
                    Node newNext = new Node(x,y);
                    newNext.parent = current_node;
                    temp.next = newNext;
                    //now = newNext;
                    //now_board[x][y] = 1;
                    return;
                }
            }
        }
    }

    public void createNext(Node temp, Node current_node, int x, int y) {
        Node newNext = new Node(x,y);
        newNext.parent = current_node;
        temp.next = newNext;
    }

    public Node getHead() {
        return head;
    }

    public Node getNow() {
        return now;
    }

    public int[][] getNow_board() {
        return now_board;
    }

    public void setNow(Node now) {
        this.now = now;
    }

    public void setNow_boardTo0() {
        for(int i=0;i<=14;i++){
            for(int j=0;j<=14;j++){
                now_board[i][j] = 0;
            }
        }
    }

    public void setText_box(String text_box) {
        this.text_box = text_box;
    }

    public String getText_box() {
        return text_box;
    }
}
