package edu.cmu.sphinx.demo.miniproject1tut9t4;



import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;


import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.util.props.ConfigurationManager;

public class GermanLetters {
	static int score = 0;
	private JFrame ourFrame;
	private JPanel controlPanel;
	private JPanel resultPanel;
	private JProgressBar progressBar;
	long startTime;
	long endTime;
	long elapsedTime;
	boolean speaking;

	static final int NUMBER_OF_TRIALS = 1;

	JButton startRec;
	JLabel resultOriginalLabel;
	JLabel resultOriginalLabelValue;
	JLabel resultRecognizedLabel;
	JLabel resultRecognizedLabelValue;
	JLabel resultScoreLabel;
	JLabel resultScoreLabelValue;
	JLabel resultTimeLabel;
	JLabel resultTimeLabelValue;

	JLabel levelNoLabel;
	JLabel levelNoLabelValue;
	
	TimeFrame frame;

	boolean firstClick = true;
	Recognizer recognizer;

	ArrayList<String[]> levels = new ArrayList<>();
	String[][] recognizedWord = new String[2][20];


	static String[] level_1 = { "Hallo", "Bluse", "Hund", "Aufwiedersehen",
		"Tschuss", "danke", "bitte", "schÖn", "schlafen", "bett", "mund",
		"hose", "nase", "schuhe", "singen", "sonne", "mond", "sterne",
		"wolken", "himmel" };

	static String[] level_2 = { "arbeiten", "richtig", "klar", "spaß",
			"viel", "welt", "haus", "katze", "essen", "möglich" };

	int currentLevelIndex = 0;

	int currentWordIndex = 0;
	int currentWordCharacterIndex = 0;
	String currentWord;
	int letter_trials = 0;

	public GermanLetters() {
		levels.add(level_1);
		levels.add(level_2);
		createGUI();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ConfigurationManager cm = new ConfigurationManager(
						GermanLetters.class
								.getResource("germanletters.config.xml"));
				recognizer = (Recognizer) cm.lookup("recognizer");
				recognizer.allocate();
				Microphone microphone = (Microphone) cm.lookup("microphone");
				if (!microphone.startRecording()) {
					System.out.println("Cannot start microphone.");
					recognizer.deallocate();
					System.exit(1);
				}
			}

		});
	}

	private void createGUI() {
		ourFrame = new JFrame("german");
		ourFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ourFrame.setSize(300, 300);
		ourFrame.setLayout(new GridLayout(4, 1));
		ourFrame.setResizable(false);

		controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());

		levelNoLabel = new JLabel("Level", JLabel.CENTER);
		levelNoLabelValue = new JLabel(currentLevelIndex + 1 + " ",
				JLabel.CENTER);

		controlPanel.add(levelNoLabel);
		controlPanel.add(levelNoLabelValue);

		resultPanel = new JPanel();
		resultPanel.setLayout(new GridLayout(8, 1));
		resultPanel.setPreferredSize(new Dimension(300, 200));

		resultOriginalLabel = new JLabel("Original Word", JLabel.CENTER);

		Font font = resultOriginalLabel.getFont();
		Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
		resultOriginalLabel.setFont(boldFont);

		resultOriginalLabelValue = new JLabel(level_1[0], JLabel.CENTER);

		resultRecognizedLabel = new JLabel("Recognized Word", JLabel.CENTER);
		resultRecognizedLabel.setFont(boldFont);

		resultRecognizedLabelValue = new JLabel("--", JLabel.CENTER);

		resultScoreLabel = new JLabel("Score", JLabel.CENTER);
		resultScoreLabel.setFont(boldFont);

		resultScoreLabelValue = new JLabel("0", JLabel.CENTER);

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
		//resultPanel.add(resultTimeLabelValue);
		
		frame = new TimeFrame();
		resultPanel.add(frame);

		ourFrame.setContentPane(controlPanel);

		startRec = new JButton("Start Recording");		
		startRec.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				startRec.setEnabled(false);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (firstClick) {
							startTime = System.currentTimeMillis();
							frame.timer.startTimer();
							firstClick = false;
						}
						handleNextCharacter();
					}
				});
			}
		});

		// controlPanel.add(wordLabel);
		// controlPanel.add(inputWord);
		controlPanel.add(startRec);

		progressBar = new JProgressBar();
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		controlPanel.add(progressBar);
		ourFrame.add(resultPanel);
		ourFrame.setVisible(true);

	}

	// public void recordingState() {
	// SwingUtilities.invokeLater(new Runnable() {
	// @Override
	// public void run() {
	// // endTime = System.currentTimeMillis();
	// // elapsedTime = (endTime - startTime) / 1000;
	// //
	// // double percentage = (1.0 * score / size) * 100;
	// // System.out.println(percentage);
	// //
	// // resultOriginalLabelValue.setText(word);
	// // resultRecognizedLabelValue.setText(recognizedWord);
	// // resultScoreLabelValue.setText(Double.toString(percentage) +
	// // "%");
	// // resultTimeLabelValue.setText(elapsedTime + " s");
	// }
	//
	// });

	// }

	public void handleNextCharacter() {
		String[] currentLevel = levels.get(currentLevelIndex);
		currentWord = currentLevel[currentWordIndex];
		String currentCharacter = currentWord.charAt(currentWordCharacterIndex)
				+ "";

		String uttered = "";
		boolean correct = false;
	//	if (letter_trials < 3) {
			System.out.println("Say something (" + letter_trials + ")");
			edu.cmu.sphinx.result.Result res = recognizer.recognize();
			uttered = res.getBestFinalResultNoFiller();
			if (uttered.equalsIgnoreCase(currentCharacter)) {
				score += 10;
				correct = true;
			}
			letter_trials++;
		//}

		System.out.println(uttered + " (" + letter_trials + ")");
		if (!correct && letter_trials > 3) {
			score -= 2;
			correct = true;
		}

		if (correct) {
			letter_trials = 0;
			if (recognizedWord[currentLevelIndex][currentWordIndex] == null)
				recognizedWord[currentLevelIndex][currentWordIndex] = (String) "";
			recognizedWord[currentLevelIndex][currentWordIndex] += uttered;
			currentWordCharacterIndex++;
			resultRecognizedLabelValue
					.setText(recognizedWord[currentLevelIndex][currentWordIndex]);
			resultScoreLabelValue.setText(score+"");

			if (currentWordCharacterIndex == currentWord.length()) {
				currentWordIndex++;
				currentWordCharacterIndex = 0;
				
				
				if (currentWordIndex == currentLevel.length) {
					currentLevelIndex++;
					currentWordIndex = 0;

					if (currentLevelIndex == levels.size()) {
						currentWordIndex = levels.get(levels.size() - 1).length;
						theEnd();
					} else {
						levelNoLabelValue.setText(currentLevelIndex + 1 + " ");
						resultOriginalLabelValue.setText(levels
								.get(currentLevelIndex)[currentWordIndex]);
						resultRecognizedLabelValue.setText("--");
					}
				} else {
					resultOriginalLabelValue
							.setText(currentLevel[currentWordIndex]);
					resultRecognizedLabelValue.setText("--");
				}
				int progressValue = (int) (((1.0 * currentWordIndex) / 
						levels.get(Math.min(currentLevelIndex,levels.size() 
								- 1)).length) * 100);
				progressBar.setValue(progressValue);
				if(progressValue < 25) {
					progressBar.setForeground(Color.red);
				}
				else if(progressValue < 50) {
					progressBar.setForeground(Color.orange);
				}
				else if(progressValue < 75) {
					progressBar.setForeground(Color.yellow);
				}
				else {
					progressBar.setForeground(Color.green);
				}
				Rectangle progressRect = progressBar.getBounds();
				progressRect.x = 0;
				progressRect.y = 0;
				progressBar.paintImmediately(progressRect);
			}

		}
		resultScoreLabelValue.setText(score + "");

		// int progressValue = (int) (((1.0 * i) / size) * 100);
		// progressBar.setValue(progressValue);
		// Rectangle progressRect = progressBar.getBounds();
		// progressRect.x = 0;
		// progressRect.y = 0;
		// progressBar.paintImmediately(progressRect);
		//
		// System.out.println(progressValue);
		// System.out.println(recognizedWord);
		startRec.setEnabled(true);
	}

	private void skipCurrentCharacter() {
		// TODO Auto-generated method stub

	}

	private void theEnd() {
		System.out.println("The end");
		startRec.setVisible(false);
		resultRecognizedLabel.setText("");
		resultRecognizedLabelValue.setText("");
		resultOriginalLabel.setText("");
		resultOriginalLabelValue.setText("Super ! Das ist alles");
		/*endTime = System.currentTimeMillis();
		elapsedTime = (endTime - startTime) / 1000;
		resultTimeLabelValue.setText(elapsedTime + " s");
		*/
		frame.timer.pauseTimer();
	}

	private void levelFinished() {
		System.out.println("LevelFinished");

	}

	public static void main(String[] args) {
		new GermanLetters();
	}
}

class Timer implements Runnable {
	  private Thread runThread;
	    private boolean running = false;
	    private boolean paused = false;
	    private TimeFrame timeFrame;
	    private long summedTime = 0;

	    public Timer(TimeFrame timeFrame) {
	        this.timeFrame = timeFrame;
	    }


	    public void startTimer() {
	        running = true;
	        paused = false;
	        runThread = new Thread(this);
	        runThread.start();
	    }

	    public void pauseTimer() {
	        paused = true;
	    }

	    @Override
	    public void run() {
	        long startTime = System.currentTimeMillis();
	        while(running && !paused) {
	            timeFrame.update(summedTime + (System.currentTimeMillis() - startTime));
	        }
	        if(paused)
	            summedTime += System.currentTimeMillis() - startTime;
	        else 
	            summedTime = 0;
	    }
}

class TimeFrame extends JPanel {
	 private JLabel time = new JLabel("00:00", JLabel.CENTER);
	    Timer timer;
	    public TimeFrame(){
	        timer = new Timer(this);
	         add(time);
	    }
	    public void update(long dT){
	        time.setText(String.valueOf((dT/60000)%1000)+":"+String.valueOf((dT/1000)%1000)+","+String.valueOf((dT)%1000));
	    }
}

