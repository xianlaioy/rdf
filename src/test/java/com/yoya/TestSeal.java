package com.yoya;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class TestSeal {
	
   /**
 * @param x 印章的x方向位置
 * @param y 印章的y方向位置
 * @param border 印章的边界线厚度
 * @param width 印章的宽度
 * @param height 印章的高度
 * @param text 印章上环形展示的文本
 * @param cocangle 椭圆印章上环形展示的文本头尾产生的余角
 * @param textSize 文字的大小
 * @param srcImgPath 源图片路径
 * @param outImgPath 生成图片的路径
 * @throws IOException 
 */
public void mark(int x,int y,int border,int width,int height, String text,int cocangle,
		   int textSize,String srcImgPath, String outImgPath) throws IOException {
	   
	   //读取图片，这里的图片最好是png格式，有更好的视觉效果
       File srcImgFile = new File(srcImgPath);
       Image srcImg = ImageIO.read(srcImgFile);
       int srcImgWidth = srcImg.getWidth(null);
       int srcImgHeight = srcImg.getHeight(null);
       
       BufferedImage bufImg = new BufferedImage(srcImgWidth, srcImgHeight, BufferedImage.TYPE_INT_RGB);
       Graphics2D g = bufImg.createGraphics();
       //去除文本和椭圆的边缘锯齿感
       g.setRenderingHint(
               RenderingHints.KEY_TEXT_ANTIALIASING, 
               RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
       g.setRenderingHint(
       		RenderingHints.KEY_ANTIALIASING,
       		RenderingHints.VALUE_ANTIALIAS_ON);
       g.drawImage(srcImg, 0, 0, srcImgWidth, srcImgHeight, null);
       //设置字体，红色楷体，此处可更改
       Font font = new Font("宋体", Font.PLAIN, textSize);
       g.setColor(Color.red); 
       g.setFont(font);
       //画椭圆
       Ellipse2D el = new Ellipse2D.Double(x,y,width,height);
       g.setStroke(new BasicStroke((float) border));
       g.draw(el);
       //保留原始的变换
       AffineTransform origin = g.getTransform(); 
       //画一个子椭圆，子椭圆比原始的椭圆小一圈。
       //印章的字是写在子椭圆上的，让子椭圆贯穿每个字的字心。
       //从这里正式开始计算，记录下时间
       System.out.println("a"+System.currentTimeMillis());
       //margin的意思是字最上顶和椭圆边上的距离，也就是那一小段留白
       double margin = height/15;
       //sub_width的意思是子椭圆长半轴的长度，sub_height是子椭圆短半轴的长度
       double sub_width = (width/2 - margin - textSize/2)*0.98;
       double sub_height = height/2 - margin - textSize/2;  
       
       /*****************************************
        * 在这里简要描述一下算法：
        * 
        * 算法实现的目的是保证以下两点：
        * 1.把弧长按照文字的数量平分，记录所有平分弧长的点，这些点都在子椭圆上，让这些点成为所有字的字心。
        * 2.过这些点对子椭圆做切线，在字心旋转每个字，使得每个字都垂直于切线。
        * 
        * 为了计算椭圆的弧长，采用微积分经典的以直代曲的思想：
        * 1.把椭圆分成许多许多段，每一段都当成直线，用直线代替曲线算弧长
        * 2.分的段数越多，距离的计算越精确，也就越接近于椭圆的弧长的精确值
        * 这种方法叫做数值积分。
        * 之所以采用数值积分的方法而不是直接对原函数进行积分
        * 是因为椭圆积分的原函数不是初等函数，无法表达，所以只能采用数值方法。
        * 
        * 为了计算所有的切线，同样采用以直代曲的思想：
        * 1.找到切点附近的两个点，一前一后
        * 2.过这两个点做一条直线，直线的斜率就是切线的斜率。
        *****************************************/
       
	   //设置精度，迭代10000次，在这样的迭代次数下，可以保证计算弧长距离的时候精确到小数点后两位
	   int max = 10000;
       
       //rad是弧长所对应的角度，step是单位步长，start是起始的角度
       double rad = Math.toRadians(360 - cocangle);
       double step = rad/max;
       double start = (Math.PI + rad)/2;
       
       //创建位置数组，其中：
       //x0s记录了每个字的字心的x坐标
       //y0s记录了每个字的字心的y坐标
       //rots记录了每个字的旋转角度
       double x0s[] = new double[text.length()];
       double y0s[] = new double[text.length()];
       double rots[] = new double[text.length()];
       
	   //以下是中间变量，对最终结果没有直接意义       
       double core[] = new double[text.length()];
	   int words_count = text.length();
	   double[] length = new double[max];
	   double[] x_step = new double[max];
	   double[] y_step = new double[max];
	   double[] angles = new double[max];
	   
	   //先迭代计算弧长
	   double length_total = 0 ;
	   x_step[0] =  Math.cos(start)*sub_width + x + width/2;
       y_step[0] =  -Math.sin(start)*sub_height + y + height/2;
       length[0] =  0;
       angles[0] = start;
	   for (int i = 1; i < max; i++)
	   {
		   angles[i] = start - i*step;
		   x_step[i] =  Math.cos(angles[i])*sub_width + x + width/2;
	       y_step[i] =  -Math.sin(angles[i])*sub_height + y + height/2;  
	       length[i] =  Math.sqrt((x_step[i]-x_step[i-1])*(x_step[i]-x_step[i-1]) +
	    		        (y_step[i]-y_step[i-1])*(y_step[i]-y_step[i-1]));
	       length_total = length_total + length[i-1];
	   }
	   
	   //计算位置
	   double length_avg = length_total/(words_count-1);
	   double length_sum = 0;
	   int j = 1;
	   //计算起始点的位置
	   core[0] = (Math.PI + rad)/2;	   
	   x0s[0] = Math.cos(core[0])*sub_width + x + width/2;
	   y0s[0] = -Math.sin(core[0])*sub_height + y + height/2;
	   //计算终点的位置
	   core[words_count-1] = ((Math.PI + rad)/2 - rad*1.01);	   
	   x0s[words_count-1] = Math.cos(core[words_count-1])*sub_width + x + width/2;
	   y0s[words_count-1] = -Math.sin(core[words_count-1])*sub_height + y + height/2;
	   //计算其它点的位置
	   for(int i = 1; i < max; i++)
	   {
		   length_sum = length_sum + length[i];
		   if(length_sum>(length_avg))
		   {
			   length_sum = 0;
			   core[j] = angles[i]-rad*0.01/words_count*j;
			   x0s[j] = x_step[i];
			   y0s[j] = y_step[i];
			   j=j+1;
			   if(j == words_count)
				   break;
		   }
	   }
       
       //在这里计算旋转角度
       for (int i = 0; i < words_count; i++)
       {
    	   	double x_point_before = Math.cos(core[i]+0.01)*sub_width + x + width/2;
    	   	double x_point_after = Math.cos(core[i]-0.01)*sub_width + x + width/2;
    	   	double y_point_before = -Math.sin(core[i]+0.01)*sub_height + y + height/2;
    	   	double y_point_after = -Math.sin(core[i]-0.01)*sub_height + y + height/2;
    	   	double slope = (y_point_after - y_point_before)/(x_point_after - x_point_before);
    	   	rots[i] = 2*Math.PI + Math.atan(slope);
    	   	if(Math.sin(core[i])<0)
    	   	{
    	   		rots[i]=rots[i]+Math.PI;
    	   	}
       }
       //这里运算完成了，再记录一下时间
       System.out.println("a"+System.currentTimeMillis());
       
       //打印文字，值得注意的是，java在打印文字的时候，并非从左下角开始打印。
       //而是略略偏上于左下角，这个textSize*0.15就是为了修正这个距离
       //此外还要考虑字本身的大小，textSize/2就是为了修正这个问题
       for (int i = 0; i < words_count; i++)
       {
    	   //x0是横坐标，y0是纵坐标，rot是旋转角度 
    	    g.setTransform(origin);
    	    g.rotate(rots[i], x0s[i], y0s[i]);
            g.scale(0.8,1.2);
    	    g.drawString(text.substring(i, i+1), (int)((x0s[i] - (double)textSize/2)/0.8),
    	    		(int)((y0s[i] + (double)textSize/2 - (double)textSize*0.15)/1.2));    	    
       }
       
       g.dispose();
       // 输出图片
       FileOutputStream outImgStream = new FileOutputStream(outImgPath);
       ImageIO.write(bufImg, "png", outImgStream);
       outImgStream.flush();
       outImgStream.close();
}
	
   public static void main(String[] args) throws Exception {
	//厦门优芽网络科技有限公司印
	System.out.println(	System.currentTimeMillis());
	new TestSeal().mark(100,100,2,220,120,"厦门优芽网络科技有限公司",190,16,"E:/Seal.png", "E:/Seal.png");
	System.out.println(	System.currentTimeMillis());
   }
   
}
