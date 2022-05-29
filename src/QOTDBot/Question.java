package QOTDBot;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

public class Question {
	private String question;
	private String footer;
	private User author;
	private String date;

	public Question(String q, String f, User a) {
		setQuestion(q);
		setFooter(f);
		setAuthor(a);
		updateDate();
	}
	public Question(String q, User a) {
		setQuestion(q);
		setFooter("");
		setAuthor(a);
		updateDate();
	}

	
	public void updateDate() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		String now = LocalDateTime.now().format(dtf);
		date = now;
	}
	public void setAuthor(User a) {
		author = a;
	}
	public void setFooter(String f) {
		footer = f;
	}
	public void setQuestion(String q) {
		question = q;
	}
	
	private String getDate() {
		return date;  
	}
	private User getAuthor() {
		return author;
	}
	private String getFooter() {
		return footer;
	}
	public String getQuestion() {
		return question;
	}
	
	public MessageEmbed createEmbed() {
		EmbedBuilder QOTDEmbed = new EmbedBuilder();
		QOTDEmbed.setAuthor("Added by: " + getAuthor().getAsTag(), null, getAuthor().getAvatarUrl())
		.setTitle("QOTD For Today!\n**Question:** " + getQuestion())
		.setDescription("*" + getFooter() + "*")
		.setFooter("Added on: " + getDate())
		.setColor(new Color(230, 33, 39));
		return QOTDEmbed.build();
	}
	public String toString() {
		return "**Question:** " + getQuestion() + "\n**Footer:** " + getFooter() + "\n**Author:** " + getAuthor().getAsTag() + "\n**Date:** " + getDate();
	}
	
}
