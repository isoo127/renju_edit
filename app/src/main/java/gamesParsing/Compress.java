package gamesParsing;

import java.util.BitSet;

import io.realm.RealmList;

public class Compress {

    static public RealmList<byte[]> toByteArrayList(String move) {
        String[] text = move.split("\\s");
        int[][] board = new int[15][15];
        RealmList<byte[]> list = new RealmList<>();

        for(int i = 0; i < text.length; i++) {
            String num = text[i].replaceAll("[^\\d]", "");
            int x = word2num(text[i].charAt(0));
            int y = Integer.parseInt(num) - 1;

            if((i % 2) == 0) // mean black
                board[x][y] = 1;
            else // mean white
                board[x][y] = -1;

            int[][] compressArr = smallArr(board);
            byte[] byteArr = intArr2Byte(compressArr);
            list.add(byteArr);
        }

        return list;
    }

    static public byte[][] toByteArray(String move) {
        String[] text = move.split("\\s");
        int[][] board = new int[15][15];

        int x = 0, y = 0;
        for(int i = 0; i < text.length; i++) {
            String num = text[i].replaceAll("[^\\d]", "");
            x = word2num(text[i].charAt(0));
            y = Integer.parseInt(num) - 1;
            if((i % 2) == 0) // mean black
                board[x][y] = 1;
            else // mean white
                board[x][y] = -1;
        }

        int[][] compressArr = smallArr(board);
        byte[][] byteArr = new byte[8][];

        for(int p = 0; p < 2; p++) {
            for (int k = 0; k < 4; k++) {
                byteArr[4 * p + k] = intArr2Byte(compressArr);

                for (int i = 0; i < compressArr.length / 2; i++) {
                    for (int j = i; j < compressArr.length - i - 1; j++) {
                        int temp = compressArr[i][j];
                        int temp2 = compressArr[j][compressArr.length - i - 1];
                        compressArr[i][j] = compressArr[compressArr.length - j - 1][i];
                        compressArr[j][compressArr.length - i - 1] = temp;
                        temp = compressArr[compressArr.length - i - 1][compressArr.length - j - 1];
                        compressArr[compressArr.length - i - 1][compressArr.length - j - 1] = temp2;
                        compressArr[compressArr.length - j - 1][i] = temp;
                    }
                }
            }

            for (int i = 0; i < compressArr.length / 2; i++) {
                for (int j = 0; j < compressArr.length; j++) {
                    int temp = compressArr[i][j];
                    compressArr[i][j] = compressArr[compressArr.length - i - 1][j];
                    compressArr[compressArr.length - i - 1][j] = temp;
                }
            }
        }
        return byteArr;
    }

    static private int word2num(char c) {
        int num = c - 97;
        return num;
    }

    static private byte[] intArr2Byte(int[][] board) {
        byte[] b;

        BitSet bitSet = new BitSet();
        int index = 0;
        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board.length; j++) {
                if(board[i][j] == 0) {
                    index++;
                } else if(board[i][j] == 1) {
                    bitSet.set(index);
                    index++;
                    index++;
                } else if(board[i][j] == -1) {
                    bitSet.set(index);
                    index++;
                    bitSet.set(index);
                    index++;
                }
            }
        }

        b = bitSet.toByteArray();
        return b;
    }

    static private int[][] smallArr(int[][] board) {
        if(board.length == 1)
            return board;

        boolean flag = false;
        for(int i = 0; i < board.length; i++) {
            if (board[i][0] != 0)
                flag = true;
            else if (board[0][i] != 0)
                flag = true;
            else if (board[board.length - 1][i] != 0)
                flag = true;
            else if (board[i][board.length - 1] != 0)
                flag = true;

            if(flag)
                return board;
        }

        int[][] board2 = new int[board.length - 2][board.length - 2];

        for(int i = 0; i < board2.length; i++) {
            for(int j = 0; j < board2.length; j++) {
                board2[i][j] = board[i + 1][j + 1];
            }
        }

        return smallArr(board2);
    }

}
