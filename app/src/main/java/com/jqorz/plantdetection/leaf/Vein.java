package com.jqorz.plantdetection.leaf;

import android.graphics.Bitmap;
import android.graphics.Color;


/**
 * 计算纹理特征
 */
public class Vein {
    private Bitmap Oimage;
    private double[] H = {0, 0, 0, 0};
    private double[] I = {0, 0, 0, 0};
    private double[] E = {0, 0, 0, 0};
    private double[] C = {0, 0, 0, 0};
    private double[] Ux = {0, 0, 0, 0};
    private double[] Uy = {0, 0, 0, 0};
    private double[] deltaX = {0, 0, 0, 0};
    private double[] deltaY = {0, 0, 0, 0};
    private double[][][] Coccurrence = new double[16][16][4];
    private double[][][] normalized = new double[16][16][4];
    private double[] T = new double[8];
    private int wd, ht;

    private int[] pixels;
    private int[][] pixels1;

    public Vein(Bitmap image) {

        Oimage = image;
        wd = Oimage.getWidth();
        ht = Oimage.getHeight();

        //将图像转为一维矩阵
        pixels = grabber(Oimage, wd, ht);
        //矩阵灰度均值化
        pixels = toGray(pixels, wd, ht);

        //将一维灰度图像转为二维图像
        pixels1 = toGray2d(pixels, wd, ht);

        //将二维灰度图像转为16位灰度图像
        pixels1 = toGray(pixels1, wd, ht, 16);

        //得到16位灰度图像的共生矩阵
        Coccurrence = Coccurrence_matrix(pixels1, wd, ht);

        //矩阵正常化
        normalized = Matrix_normalized(Coccurrence);

        //特征提取
        Feature_extraction(normalized);
    }

    //一维数组转为二维
    public int[][] toGray2d(int[] pix, int iw, int ih) {
        int[][] pixs = new int[iw][ih];
        int k = 0;
        for (int i = 0; i < iw; i++)
            for (int j = 0; j < ih; j++) {
                pixs[i][j] = pix[k];
                k++;
            }
        return pixs;
    }


    //将图像的数组转为灰度
    public int[][] toGray(int[][] pix, int iw, int ih, int a) {
        for (int i = 0; i < iw; i++)
            for (int j = 0; j < ih; j++)
                for (int n = 1; n <= 256 / a; n++) {
                    if ((n - 1) * a <= pix[i][j] && pix[i][j] <= (n - 1) * a + 15) {
                        pix[i][j] = n - 1;
                    }
                }
        return pix;
    }


    // 将RGB转为平均灰度值
    public int[] toGray(int[] pix, int iw, int ih) {
        int r, g, b, gray;

        for (int i = 0; i < iw * ih; i++) {
            r = Color.red(pix[i]);
            g = Color.green(pix[i]);
            b = Color.blue(pix[i]);
            gray = ((r + g + b) / 3);
            pix[i] = gray;
        }
        return pix;
    }

    /**
     * 将原图片转为一维矩阵
     *
     * @param im Bitmap
     * @param iw 宽度
     * @param ih 高度
     * @return 一维矩阵
     */
    public int[] grabber(Bitmap im, int iw, int ih) {
        int[] pix = new int[900000];
        ColorUtil.getPixels(im, 0, 0, iw, ih, pix);
        return pix;
    }

    //生成a*b*c的零矩阵
    public double[][][] zeros(int a, int b, int c) {
        double[][][] pix = new double[a][b][c];
        for (int i = 0; i < a; i++)
            for (int j = 0; j < b; j++)
                for (int k = 0; k < c; k++)
                    pix[i][j][k] = 0;
        return pix;
    }

    //共生矩阵
    public double[][][] Coccurrence_matrix(int[][] pix, int iw, int ih) {
        double[][][] p = zeros(16, 16, 4);
        for (int m = 0; m < 16; m++)
            for (int n = 0; n < 16; n++) {
                for (int i = 1; i < iw - 1; i++)
                    for (int j = 1; j < ih - 1; j++) {

                        if (j < ih && pix[i][j] == m && pix[i][j + 1] == n) {
                            p[m][n][0] = p[m][n][0] + 1;
                            p[n][m][0] = p[m][n][0];
                        }

                        if (j > 1 && j < ih && pix[i][j] == m && pix[i - 1][j + 1] == n) {
                            p[m][n][1] = p[m][n][1] + 1;
                            p[n][m][1] = p[m][n][1];
                        }

                        if (j < iw && pix[i][j] == m && pix[i + 1][j] == n) {
                            p[m][n][2] = p[m][n][2] + 1;
                            p[n][m][2] = p[m][n][2];
                        }

                        if (j < iw && j < ih && pix[i][j] == m && pix[i + 1][j + 1] == n) {
                            p[m][n][3] = p[m][n][3] + 1;
                            p[n][m][3] = p[m][n][3];
                        }
                    }
                if (m == n)
                    for (int x = 0; x < 4; x++)
                        p[m][n][x] = p[m][n][x] * 2;
            }
        return p;
    }

    //矩阵正常化
    public double[][][] Matrix_normalized(double[][][] pix) {
        double[] a = new double[4];
        double sum = 0;
        for (int n = 0; n < 4; n++) {
            for (int i = 0; i < 16; i++)
                for (int j = 0; j < 16; j++) {
                    sum += pix[i][j][n];
                }
            a[n] = sum;
            sum = 0;
        }
        for (int n = 0; n < 4; n++)
            for (int i = 0; i < 16; i++)
                for (int j = 0; j < 16; j++)
                    pix[i][j][n] = pix[i][j][n] / a[n];
        return pix;
    }

    //特征提取
    public void Feature_extraction(double[][][] pix) {
        double sum = 0;

        for (int n = 0; n < 4; n++) {
            for (int i = 0; i < 16; i++)
                for (int j = 0; j < 16; j++) {
                    sum += Math.pow(pix[i][j][n], 2);
                }
            E[n] = sum;
            sum = 0;

            for (int i = 0; i < 16; i++)
                for (int j = 0; j < 16; j++) {
                    if (pix[i][j][n] != 0)
                        H[n] = -pix[i][j][n] * Math.log(pix[i][j][n]) + H[n];
                    I[n] = Math.pow((i - j), 2) * pix[i][j][n] + I[n];
                    Ux[n] = i * pix[i][j][n] + Ux[n];
                    Uy[n] = j * pix[i][j][n] + Uy[n];
                }
        }
        for (int n = 0; n < 4; n++) {
            for (int i = 0; i < 16; i++)
                for (int j = 0; j < 16; j++) {
                    deltaX[n] = Math.pow((i - Ux[n]), 2) * pix[i][j][n] + deltaX[n];
                    deltaY[n] = Math.pow((i - Uy[n]), 2) * pix[i][j][n] + deltaY[n];
                }
            C[n] = (C[n] - Ux[n] * Uy[n]) / deltaX[n] / deltaY[n];
        }

        T[0] = (E[0] + E[1] + E[2] + E[3]) / 4;
        T[1] = Math.sqrt(((Math.pow((E[0] - T[0]), 2) + Math.pow((E[1] - T[0]), 2) + Math.pow((E[2] - T[0]), 2) + Math.pow((E[3] - T[0]), 2)) / 4));
        T[2] = (H[0] + H[1] + H[2] + H[3]) / 4;
        T[3] = Math.sqrt(((Math.pow((H[0] - T[2]), 2) + Math.pow((H[1] - T[2]), 2) + Math.pow((H[2] - T[2]), 2) + Math.pow((H[3] - T[2]), 2)) / 4));
        T[4] = (I[0] + I[1] + I[2] + I[3]) / 4;
        T[5] = Math.sqrt(((Math.pow((I[0] - T[4]), 2) + Math.pow((I[1] - T[4]), 2) + Math.pow((I[2] - T[4]), 2) + Math.pow((I[3] - T[4]), 2)) / 4));
        T[6] = (C[0] + C[1] + C[2] + C[3]) / 4;
        T[7] = Math.sqrt(((Math.pow((C[0] - T[6]), 2) + Math.pow((C[1] - T[6]), 2) + Math.pow((C[2] - T[6]), 2) + Math.pow((C[3] - T[6]), 2)) / 4));
    }

    public double[] getT() {
        return T;
    }
//    public static void main(String[] args) throws IOException {
//    	File a=new File("D:/Bunny/Downloads/Leaves(ICL)/001/001 (9).jpg");
//    	Image image=ImageIO.read(a);
//		wenli x=new wenli(image);
//		double[] T=x.getT();
//		for (int i = 0; i < 8; i++) {
//			System.out.println(T[i]);
//			
//		}
//		
//	}
}