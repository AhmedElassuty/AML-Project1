package edu.cmu.sphinx.demo.miniproject1tut9t4;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.util.props.ConfigurationManager;

public class GermanLetters {
	static String word = "Hallo";
	static int score = 0;
	private JFrame ourFrame;
	private JPanel controlPanel;
	private JPanel resultPanel;
	private JProgressBar progressBar;
	long startTime;
	long endTime;
	long elapsedTime;
	boolean speaking;
	static String recognizedWord = "";
	JLabel resultOriginalLabel;
	JLabel resultOriginalLabelValue;
	JLabel resultRecognizedLabel;
	JLabel resultRecognizedLabelValue;
	JLabel resultScoreLabel;
	JLabel resultScoreLabelValue;
	JLabel resultTimeLabel;
	JLabel resultTimeLabelValue;

	public GermanLetters() {
		ourFrame = new JFrame("german");
		ourFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ourFrame.setSize(300, 300);
		ourFrame.setLayout(new GridLayout(3, 1));
		ourFrame.setResizable(false);

		controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());

		resultPanel = new JPanel();
		resultPanel.setLayout(new GridLayout(8, 1));
		resultPanel.setPreferredSize(new Dimension(300, 200));

		resultOriginalLabel = new JLabel("Original Word", JLabel.CENTER);
		
		Font font = resultOriginalLabel.getFont();
		Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
		resultOriginalLabel.setFont(boldFont);
		
		resultOriginalLabelValue = new JLabel("--", JLabel.CENTER);

		resultRecognizedLabel = new JLabel("Recognized Word", JLabel.CENTER);
		resultRecognizedLabel.setFont(boldFont);
		
		
		resultRecognizedLabelValue = new JLabel("--", JLabel.CENTER);

		resultScoreLabel = new JLabel("Score", JLabel.CENTER);
		resultScoreLabel.setFont(boldFont);
		
		
		resultScoreLabelValue = new JLabel("--", JLabel.CENTER);

		resultTimeLabel = new JLabel("Elapsed Time", JLabel.CENTER);
		resultTimeLabel.setFont(boldFont);
		
		resultTimeLabelValue = new JLabel("--", JLabel.CENTER);

		Border border1 = BorderFactory.createTitledBorder("Result");
		resultPanel.setBorder(border1);

		resultPanel.add(resultOriginalLabel);
		resultPanel.add(resultOriginalLabelValue);
		resultPanel.add(resultRecognizedLabel);
		resultPanel.add(resultRecognizedLabelValue);

		resultPanel.add(resultScoreLabel);
		resultPanel.add(resultScoreLabelValue);

		resultPanel.add(resultTimeLabel);
		resultPanel.add(resultTimeLabelValue);

		// ourFrame.add(headerLabel);
		ourFrame.setContentPane(controlPanel);
		// ourFrame.add(statusLabel);

		JLabel wordLabel = new JLabel("German Word: ", JLabel.CENTER);
		final JTextField inputWord = new JTextField(15);

		JButton startRec = new JButton("Start Recording");

		startRec.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				word = inputWord.getText();
				recognizedWord = "";
				startTime = System.currentTimeMillis();
				recordingState();
			}
		});

		controlPanel.add(wordLabel);
		controlPanel.add(inputWord);
		controlPanel.add(startRec);

		progressBar = new JProgressBar();
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		controlPanel.add(progressBar);
		ourFrame.add(resultPanel);
		ourFrame.setVisible(true);
	}

	public void recordingState() {
		progressBar.setValue(0);
		resultOriginalLabelValue.setText("--");
		resultRecognizedLabelValue.setText("--");
		resultScoreLabelValue.setText("--");
		resultTimeLabelValue.setText("--");

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				ConfigurationManager cm = new ConfigurationManager(
						GermanLetters.class
								.getResource("germanletters.config.xml"));
				Recognizer recognizer = (Recognizer) cm.lookup("recognizer");
				recognizer.allocate();
				Microphone microphone = (Microphone) cm.lookup("microphone");
				if (!microphone.startRecording()) {
					System.out.println("Cannot start microphone.");
					recognizer.deallocate();
					System.exit(1);
				}

				int i = 0;
				int size = word.length();

				while (i < size) {
					System.out.println("Say something");
					edu.cmu.sphinx.result.Result res = recognizer.recognize();
					String uttered = res.getBestFinalResultNoFiller();
					recognizedWord += uttered;
					if (uttered.equalsIgnoreCase("" + word.charAt(i)))
						score++;
					i++;
					int progressValue = (int) (((1.0 * i) / size) * 100);
					progressBar.setValue(progressValue);
					Rectangle progressRect = progressBar.getBounds();
					progressRect.x = 0;
					progressRect.y = 0;
					progressBar.paintImmediately(progressRect);

					System.out.println(progressValue);
					System.out.println(recognizedWord);

				}

				endTime = System.currentTimeMillis();
				elapsedTime = (endTime - startTime) / 1000;

				double percentage = (1.0 * score / size) * 100;
				System.out.println(percentage);

				resultOriginalLabelValue.setText(word);
				resultRecognizedLabelValue.setText(recognizedWord);
				resultScoreLabelValue.setText(Double.toString(percentage) + "%");
				resultTimeLabelValue.setText(elapsedTime + " s");
			}

		});

	}

	public static void main(String[] args) {
		new GermanLetters();
	}
}
