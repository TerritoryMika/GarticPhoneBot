import java.awt.AWTException;
import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class PhoneBot {
	public static final Color[] COLORS  = new Color[] {
		// Gartic Phone provided color
		  new Color(0  ,0  ,0  ), // black
	    new Color(102,102,102), // gray
	    new Color(0  ,80 ,205), // blue
	    new Color(255,255,255), // white
	    new Color(170,170,170), // gray2
	    new Color(38 ,201,255), // blue2
	    new Color(1  ,116,32 ), // green
	    new Color(105,21 ,6  ), // brown
	    new Color(150,65 ,18 ), // brown2
	    new Color(17 ,176,60 ), // green2
	    new Color(255,0  ,19 ), // red
	    new Color(255,120,41 ), // orange
	    new Color(176,112,28 ), // brown3
	    new Color(153,0  ,78 ), // purple
	    new Color(203,90 ,87 ), // brown4
	    new Color(255,193,38 ), // brown5
	    new Color(255,0  ,143), // pink
	    new Color(254,175,168) 	// skin
	};
	
	public static GPixel[] altcolors = new GPixel[COLORS.length * 10];
	
	public static void initAltColor() {
		int pos = 0;
		for(var gc : GColor.values())
			for(var go : GOpacity.values()) {
					altcolors[pos] = new GPixel(formColor(gc, go), gc, go);
					pos++;
				}
		System.out.println("Log : Color collection set up finished");
	}
	
	public static List<MouseOperation> generateSequence(String path) {
		var result = new LinkedList<MouseOperation>();
		try {
			var cgc = GColor.Color_black;
			var cgo = GOpacity.Opacity_100;
			var bi = new BufferedImage(PANEL_W / PanelXGrid, PANEL_H / PanelYGrid, BufferedImage.TYPE_INT_RGB);
			bi.getGraphics().drawImage(ImageIO.read(new File(path)), 0, 0,  bi.getWidth(), bi.getHeight(), null);
			for(int x = 0; x < bi.getWidth(); x++)
				for(int y = 0; y < bi.getHeight(); y++) {
					var p = toPixel(new Color(bi.getRGB(x, y)));
					if(property_skipWhite && p.color == GColor.Color_white) {
						continue;
					}
					if(p.color != cgc) {
						result.add(new MouseOperation.Move(p.color.getLocation(), "color change"));
						result.add(new MouseOperation.Click());
						cgc = p.color;
					}
					if(p.opacity != cgo) {
						result.add(new MouseOperation.Move(p.opacity.getLocation(), "opacity change"));
						result.add(new MouseOperation.Click());
						cgo = p.opacity;
					}
					result.add(new MouseOperation.Move(PANEL_X + PanelXGrid * x, PANEL_Y + PanelYGrid * y));
					result.add(new MouseOperation.Click());
				}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static int PanelXGrid = 7;
	public static int PanelYGrid = 6;
	
	public static int delay = 8;
	
	public static final int PANEL_X = {_};
	public static final int PANEL_Y = {_};
	public static final int PANEL_W = {_};
	public static final int PANEL_H = {_};
	
	public static boolean property_skipWhite = true;
	public static ColorCompare property_colorCompareMethod = ColorCompare.Fast;
	
	public static String defaultDirectory = "";
	
	static {
		initAltColor();
	}
	
	public static void main(String[] args) throws AWTException {
		var input = new Scanner(System.in);
		var bot = new PausableRobot();
		var storage = new HashMap<String, List<MouseOperation>>();
		var exit = false;
		while(input.hasNextLine()) {
			bot.pause = false;
			var line = input.nextLine();
			var commands = line.trim().split(" ");
			switch(commands[0]) {
				case "#x" :
					if(commandLengthChecks(commands, 2)) {
						PanelXGrid = Integer.valueOf(commands[1]);
						System.out.println("Drawing pixel width : " + PanelXGrid);
					}
					break;
				case "#y" :
					if(commandLengthChecks(commands, 2)) {
						PanelYGrid = Integer.valueOf(commands[1]);
						System.out.println("Drawing pixel height : " + PanelYGrid);
					}
					break;
				case "#delay" :
					if(commandLengthChecks(commands, 2)) {
						delay = Integer.valueOf(commands[1]);
						System.out.println("Current operation delay : " + delay + "ms");
					}
					break;
				case "#skipw" :
					if(commandLengthChecks(commands, 1)) {
						property_skipWhite = !property_skipWhite;
						System.out.println("Skipping white : " + property_skipWhite);
					}
					break;
				case "#dir" :
					if(commandLengthChecks(commands, 2)) {
						defaultDirectory = commands[1];
						System.out.println("Current directory : " + defaultDirectory);
					}
					break;
				case "#mode" :
					if(commandLengthChecks(commands, 2)) {
						property_colorCompareMethod = ColorCompare.valueOf(commands[1]);
					}
					break;
				case "list" :
					if(commandLengthChecks(commands, 1)) {
						storage.forEach((k, i) -> System.out.println(" - " + k + " ( steps : " + i.size() + " )"));
						System.out.println("There are " + storage.size() + " stored image");
					}
					break;
				case "gen" :
					if(commandLengthChecks(commands, 3)) {
						storage.put(commands[1], generateSequence(defaultDirectory + commands[2]));
						System.out.println("Stored " + commands[2] + " as " + commands[1]);
					}
					break;
				case "call" :
					if(commandLengthChecks(commands, 2)) {
						storage.get(commands[1]).forEach(op -> {
							if(!bot.pause) {
								try {
									op.execute(bot);
									Thread.sleep(delay);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						});
						System.out.println("Drawing " + commands[1]);
					}
					break;
				case "draw" :
					if(commandLengthChecks(commands, 2)) {
						generateSequence(defaultDirectory + commands[1]).forEach(op -> {
							if(!bot.pause) {
								try {
									op.execute(bot);
									Thread.sleep(delay);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						});
						System.out.println("Drawing " + commands[1]);
					}
					break;
				case "exit" :
					if(commandLengthChecks(commands, 1))
						exit = true;
					break;
				default : 
					System.out.println("Error : Unknown command");
			}
			if(exit)
				break;
		}
		input.close();
	}
	
	public static boolean commandLengthChecks(String[] arr, int len) {
		if(arr.length == len)
			return true;
		System.out.println("Error : Mismatch parameter counts");
		return false;
	}
	

	enum GColor {
		Color_black, Color_gray, Color_blue, Color_white, Color_gray2, Color_blue2, Color_green, Color_brown, Color_brown2, Color_green2, Color_red, Color_orange, Color_brown3, Color_purple, Color_brown4, Color_brown5, Color_pink, Color_skin;
    public static final int X   = {_};
		public static final int Y   = {_};
		public static final int DX  = {_};
		public static final int DY  = {_};
		public static final int ROW = 3;
		public Point getLocation() {
			int index = this.ordinal();
			return new Point(X + (DX * (index % ROW)), Y + (DY * (index / ROW)));
		}
	}
	
	enum GOpacity {
		Opacity_10, Opacity_20, Opacity_30, Opacity_40, Opacity_50, Opacity_60, Opacity_70, Opacity_80, Opacity_90, Opacity_100;
		public final static int Y     = {_};
		public final static int X_MIN = {_};
		public final static int X_MAX = {_};
		public Point getLocation() {
			return new Point(X_MIN + (int) ((X_MAX - X_MIN) * this.toPercentage()), Y);
		}
		public float toPercentage() {
			return (this.ordinal() + 1) * 0.1f;
		}
	}
	
	public static Color formColor(GColor gc, GOpacity go) {
		var oc = COLORS[gc.ordinal()];
		var hsv = Color.RGBtoHSB(oc.getRed(), oc.getGreen(), oc.getBlue(), null);
		return Color.getHSBColor(hsv[0], hsv[1] * go.toPercentage(), hsv[2]);
	}
	
	public static GPixel toPixel(Color pixel) {
		var closestColor = GColor.Color_black;
		var closestOpacity = GOpacity.Opacity_100;
		var closest = Double.MAX_VALUE;
		for(var ac : altcolors) {
			var nc = ac.c;
			var newclose = 0.0;
			switch(property_colorCompareMethod) {
				case Detail:
					newclose = toPixel_detailCompare(pixel, nc);
					break;
				case Fast:
					newclose = toPixel_fastCompare(pixel, nc);
					break;
				case HSV:
					newclose = toPixel_hsvCompare(pixel, nc);
					break;
			}
			if(newclose < closest) {
				closestColor = ac.color;
				closestOpacity = ac.opacity;
				closest = newclose;
			}
		}
		return new GPixel(pixel, closestColor, closestOpacity);
	}
		
	enum ColorCompare {
		Fast, HSV, Detail;
		public String toString() {
			var result = "";
			switch(this) {
				case Detail:
					result = "Detail";
					break;
				case Fast:
					result = "Fast";
					break;
				case HSV:
					result = "HSV";
					break;
			}
			return result;
		}
	}
	
	public static double toPixel_fastCompare(Color a, Color b) {
		return Math.abs(a.getRed() - b.getRed()) * Math.abs(a.getRed() - b.getRed()) * Math.abs(a.getRed() - b.getRed());
	}
	
	public static double toPixel_hsvCompare(Color a, Color b) {
		float[] hsvA = Color.RGBtoHSB(a.getRed(), a.getGreen(), a.getBlue(), null);
		float[] hsvB = Color.RGBtoHSB(b.getRed(), b.getGreen(), b.getBlue(), null);
		return Math.abs(hsvA[0] - hsvB[0]) * Math.abs(hsvA[1] - hsvB[1]) * Math.abs(hsvA[2] - hsvB[2]);
	}
	
	public static double toPixel_detailCompare(Color a, Color b) {  // https://www.compuphase.com/cmetric.htm
		double dr = a.getRed() - b.getRed();
		double dg = a.getGreen() - b.getGreen();
		double db = a.getBlue() - b.getBlue();
		double r = (a.getRed() + b.getRed()) / 2;
		return Math.sqrt((2 + r / 256) * dr * dr + 4 * dg * dg + (2 + (255 - r) / 256) * db * db);
	}
	
	public static class GPixel {
		Color c;
		GColor color;
		GOpacity opacity;
		public GPixel(Color c, GColor color, GOpacity opacity) {
			this.c = c;
			this.color = color;
			this.opacity = opacity;
		}
	}
	
	static class PausableRobot extends Robot {
		boolean pause = false;
		public PausableRobot() throws AWTException {
			super();
		}
	}
	
	static interface MouseOperation {
		public static class Move implements MouseOperation {
			static final int pauseX = {_};
			int x, y;
			String des = "";
			public Move(int x, int y) {
				this.x = x; this.y = y;
			}
			public Move(Point p, String des) {
				this.x = p.x; this.y = p.y; this.des = des;
			}
			public void execute(PausableRobot bot) {
				var mouse = MouseInfo.getPointerInfo().getLocation();
				
				if(mouse.x > pauseX) {
					bot.mouseMove(x, y);
				}else {
					System.out.println("Interrupted");
					bot.pause = true;
				}
			}
		}
		public static class Click implements MouseOperation {
			public void execute(PausableRobot bot) {
				bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			}
		}
		public void execute(PausableRobot bot);
	}
}
