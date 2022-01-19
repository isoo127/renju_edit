package com.renju_note.isoo;

import java.io.Serializable;

public class SeqTree implements Serializable {
    private static final long serialVersionUID = 2765198187236704398L;

    private final Node head = new Node(-1, -1);
    private Node now;
    private final int[][] now_board = new int[15][15];

    // for released version 1.2.3
    private final String text_box = null;

    public SeqTree() {
        this.now = this.head;
    }

    public static class Node implements Serializable{
        private static final long serialVersionUID = -6403100239045118824L;

        private final int x;
        private final int y;
        private Node chlid;
        private Node next;
        private Node parent;

        private String text;
        private String boxText;

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

        public Node getChild() {
            return chlid;
        }

        public Node getNext() { return  next; }

        public Node getParent() {
            return parent;
        }

        public void setChild(Node chlid) {
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

        public void setBoxText(String boxText) {
            this.boxText = boxText;
        }

        public String getBoxText() {
            return boxText;
        }
    }

    public void next(Node current_node, int x, int y, int nowSequence) {
        if(current_node == null || current_node.getChild() == null) {
            Node newChild = new Node(x, y);
            if (head.getChild() == null) {
                head.chlid = newChild;
                newChild.parent = head;
                current_node = head;
            }
            newChild.parent = current_node;
            current_node.chlid = newChild;
            now = newChild;
            if((nowSequence % 2) == 0)
                now_board[x][y] = 1;
            else
                now_board[x][y] = -1;
        } else {
            for(Node temp = current_node.chlid;temp != null;temp = temp.next) {
                if(temp.x == x && temp.y == y) {
                    now = temp;
                    if((nowSequence % 2) == 0)
                        now_board[x][y] = 1;
                    else
                        now_board[x][y] = -1;
                    return;
                }
                if(temp.next == null) {
                    Node newNext = new Node(x, y);
                    newNext.parent = current_node;
                    temp.next = newNext;
                    now = newNext;
                    if((nowSequence % 2) == 0)
                        now_board[x][y] = 1;
                    else
                        now_board[x][y] = -1;
                    return;
                }
            }
        }
    }

    public void undo(Node current_node) {
        now = current_node.parent;
        now_board[current_node.x][current_node.y] = 0;
    }

    public void redo(Node current_node, int nowSequence) {
        if(current_node.chlid != null && current_node.chlid.next == null) {
            now = current_node.getChild();
            if((nowSequence % 2) == 0)
                now_board[current_node.getChild().getX()][current_node.getChild().getY()] = 1;
            else
                now_board[current_node.getChild().getX()][current_node.getChild().getY()] = -1;
        }
    }

    public void delete(Node current_node) {
        now = current_node.parent;
        if(now.chlid.next == null) {
            now.setChild(null);
        } else {
            if(now.getChild().getX() == current_node.getX() && now.getChild().getY() == current_node.getY()) {
                now.setChild(now.getChild().getNext());
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
        if(current_node == null || current_node.getChild() == null) {
            Node newChild = new Node(x, y);
            if (head.getChild() == null) {
                head.chlid = newChild;
                newChild.parent = head;
                current_node = head;
            }
            newChild.parent = current_node;
            current_node.chlid = newChild;
        } else {
            for(Node temp = current_node.chlid;temp != null;temp = temp.next) {
                if(temp.x == x && temp.y == y) {
                    return;
                }
                if(temp.next == null) {
                    Node newNext = new Node(x, y);
                    newNext.parent = current_node;
                    temp.next = newNext;
                    return;
                }
            }
        }
    }

    public void createNext(Node temp, Node current_node, int x, int y) {
        Node newNext = new Node(x, y);
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

    public String getText_box() {
        return text_box;
    }
}
