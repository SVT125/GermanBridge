package com.example.james.cardsuite;
import java.util.ArrayList;


public class Display {
	private String hand1;
	private String hand2;
	private String hand3;
	private String hand4;
	private String top;
	
	public Display(String h1, String h2, String h3, String h4, String top){
		hand1 = h1;
		hand2 = h2;
		hand3 = h3;
		hand4 = h4;
		this.top = top;
	}
	
	public static String displayTest(ArrayList <Card> h1, ArrayList <Card> h2, String top){
		String s1 = "";
		String s2 = "";
		
		for(Card c: h1){
			String card = Integer.toString(c.getCardNumber()) + c.getSuit();
			s1 = s1 + card + " ";
		}
		
		s1 = s1.substring(0, s1.length()-1);
		
		for(Card c: h2){
			String card = Integer.toString(c.getCardNumber()) + c.getSuit();
			s2 = s2 + card + " ";
		}
		
		String s = "";
		String spaces = "";
		int h1size = s1.length();
		int h2size = s2.length();
		int count = (60 - h1size)/2;
		int count2 = (60 - h2size)/2;
		int count3 = (58 - top.length())/2;
		String dashes1 = "";
		String dashes2 = "";
		
		for(int i = 0; i < count3; i++){
			spaces = spaces + " ";
		}
		
		for(int i = 0; i < count; i++){
			dashes1 = dashes1 + "-";
		}
		
		for(int i = 0; i < count2; i++){
			dashes2 = dashes2 + "-";
		}
		
		s = s + dashes1 + s1 + dashes1 + "\n";
		s = s + "|" + spaces + "  " + spaces + "|" + "\n";
		s = s + "|" + spaces + "  " + spaces + "|" + "\n";
		s = s + "|" + spaces + top + spaces + "|" + "\n";
		s = s + "|" + spaces + "  " + spaces + "|" + "\n";
		s = s + "|" + spaces + "  " + spaces + "|" + "\n";
		s = s + dashes2 + s2 + dashes2 + "\n";
		
		
		
		
		return s;
	}
	
	public static void main(String[] args) {
		String s1 = "3D, 7H, 9H, 5C, 4S, 9D, 7D, 3S";
		String s2 = "5D, 7H, 9H, 5C, 7S, 9D, 3D, 3S";
		String top = "6D";
		
		//System.out.println(displayTest(s1, s2, top));
		
	}
	
}
