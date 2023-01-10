package userInterface;
//author:1159950 Yuzhou Huo
import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagLayout;

import java.awt.GridBagConstraints;

import java.awt.Insets;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Date;
import java.awt.event.ActionEvent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import entities.Constants;
import entities.Entry;
import entities.MyDatagram;
import entities.UiStringTemplate;
import myClient.InfoShareChunk;

public class GUInterface extends JFrame implements Runnable {

	private static final long serialVersionUID = 1L;
	// private JPanel contentPane;
	private JFormattedTextField ftfSearchBox = null;
	private JTextArea textArea = null;
	private JLabel lblLastModify = null;
	private JLabel lblHeartBeat = null;
	private JButton[] btns = null;

	protected InfoShareChunk curInfoShare = null;
	protected MyDatagram lastDatagram = null;// last unfinished datagram
	protected Date lastOperation = null;
	protected Date startDate = null;

	private void setListeners() {
		for (int i = 0; i < Constants.CLIENTARR.length; i++) {
			final int tmp = i;
			btns[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						onClickGeneral(Constants.CLIENTARR[tmp]);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
		}
	}

	public GUInterface(InfoShareChunk cur) {
		this.curInfoShare = cur;
	}

	public void run() {
		while (curInfoShare.running) {
			try {
				if (curInfoShare.webState.intValue() == Constants.STOP_CONNECTION) {
					synchronized (curInfoShare) {
						try {
							this.disableInput();
							curInfoShare.wait();
							this.enableInput();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				MyDatagram curRep = curInfoShare.getReplyData(-1);
				if (curRep != null)
					this.showMessageGeneral(curRep);
				this.updateLabel(lblHeartBeat,"this client have runned:\n"+getRunTime());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

//inner Utils**************************************************************************************************
	private void disableInput() throws InvocationTargetException, InterruptedException {
		EventQueue.invokeAndWait(new Runnable() {
			public void run() {
				System.out.println("disable input");
				JOptionPane.showMessageDialog(null, "lost connection with host", "Serious Error",
						JOptionPane.ERROR_MESSAGE);
				textArea.setText("host unavailable, trying to reconnect");
				textArea.setDisabledTextColor(Color.red);
				textArea.setEditable(false);
				ftfSearchBox.setText("ERROR: host unavailable");
				ftfSearchBox.setDisabledTextColor(Color.red);
				ftfSearchBox.setEditable(false);
				for (int i = 0; i < Constants.CLIENTARR.length; i++) {
					btns[i].setEnabled(false);
				}
			}
		});
	}

	private void enableInput() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				System.out.println("enable input");
				textArea.setText("");
				textArea.setEditable(true);
				ftfSearchBox.setText("");
				ftfSearchBox.setEditable(true);
				for (int i = 0; i < Constants.CLIENTARR.length; i++) {
					btns[i].setEnabled(true);
				}
				JOptionPane.showMessageDialog(null, "connection successfully recovered");
			}
		});
	}

	private void updateLabel(JLabel curLabel, String curContent) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				curLabel.setText(curContent);
			}
		});
	}

	private String convertDate(Date d) {
		if (d == null)
			return "unavailable";
		Calendar curCalendar = Calendar.getInstance();
		curCalendar.setTime(new Date());
		String res = curCalendar.get(Calendar.DATE) + "/" + curCalendar.get(Calendar.MONTH) + "/"
				+ curCalendar.get(Calendar.YEAR) + " " + curCalendar.get(Calendar.HOUR_OF_DAY) + ":"
				+ curCalendar.get(Calendar.MINUTE) + ":" + curCalendar.get(Calendar.SECOND);

		return res;
	}

	private String getRunTime() {
		if (startDate == null)
			return "unavailable";
		Calendar curCalendar = Calendar.getInstance();
		Date diffDate = new Date(new Date().getTime() - startDate.getTime());
		curCalendar.setTime(diffDate);
		String res =  curCalendar.get(Calendar.MINUTE) + " m "
				+ curCalendar.get(Calendar.SECOND)+"s";
		return res;
	}

	private void setText(Entry e) {
		System.out.println(e.Explain);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				ftfSearchBox.setText(e.Word);
				if (e.Explain != null)
					textArea.setText(e.Explain);
			}
		});
	}

	private Entry getText(boolean getTextArea) {
		String ftfContend = null;// =ftfSearchBox.getText();
		String textAreaContent = null;
		try {
			ftfContend = ftfSearchBox.getText();
			if (getTextArea)
				textAreaContent = textArea.getText();
		} catch (NullPointerException e) {
			return null;
		}
		return new Entry(ftfContend, textAreaContent);
	}

	private Entry getAndClear() {
		String ftfContend = null;// =ftfSearchBox.getText();
		String textAreaContent = null;
		ftfContend = ftfSearchBox.getText();
		textAreaContent = textArea.getText();
		ftfSearchBox.setText("");
		textArea.setText("");
		return new Entry(ftfContend, textAreaContent);
	}

	private Entry validateAndFormat(int cmdType) {
		//read input from textarea and verify it,if success, return the result, otherwise, write the text back
		Entry curEntry = null;
		curEntry = getAndClear();
		Entry originEntry = new Entry(new String(curEntry.Word), new String(curEntry.Explain));
		if (cmdType == Constants.SEARCH || cmdType == Constants.DELETE) {
			if (curEntry == null || curEntry.Word == null || curEntry.Word.equals("")) {
				JOptionPane.showMessageDialog(null, "Please input text", "Error", JOptionPane.ERROR_MESSAGE);
				this.setText(originEntry);
				return null;
			}
			curEntry.Word = curEntry.Word.toLowerCase();
		}

		else {
			if (curEntry == null || curEntry.Word == null || curEntry.Word.equals("") || curEntry.Explain.equals("")) {
				JOptionPane.showMessageDialog(null, "Please input text", "Error", JOptionPane.ERROR_MESSAGE);
				this.setText(originEntry);
				return null;
			}
			String tmp = Entry.transformToEntry(curEntry.Explain);
			if (tmp == null) {
				JOptionPane.showMessageDialog(null, "Invaild explain! Explains should contain less than"
						+ String.valueOf(Constants.MAX_EXPLAINS_LENGTH) + " letters and '-' only\n"
						+ "If mutiple explains exists, please use [number label: explain] like 1:explain 2:explain2",
						"Error", JOptionPane.ERROR_MESSAGE);
				this.setText(originEntry);
				return null;
			} else {
				curEntry.Explain = tmp;
			}
		}
		curEntry.Word = curEntry.Word.toLowerCase();
		if (!Entry.validateKey(curEntry.Word)) {
			JOptionPane
					.showMessageDialog(
							null, "Invaild word! Words should contain less than"
									+ String.valueOf(Constants.MAX_KEY_LENGTH) + "letters and '-' only",
							"Error", JOptionPane.ERROR_MESSAGE);
			this.setText(originEntry);
			return null;
		}
		if (cmdType == Constants.SEARCH || cmdType == Constants.DELETE)
			curEntry.Explain = "";
		return curEntry;
	}

	private void onClickGeneral(int cmdType) throws InterruptedException {
		//run by all the click event
		Entry curEntry = validateAndFormat(cmdType);
		if (curEntry == null)
			return;
		this.lastOperation = curEntry.Timestamp;
		long curReqNo = curInfoShare.requestNumber.getAndIncrement();
		MyDatagram msg = new MyDatagram();
		msg.clientNumber = curInfoShare.clientNo;
		msg.command = cmdType;
		msg.data = curEntry;
		msg.requestNumber = curReqNo;
		msg.state = 0;
		this.lastDatagram = msg;
		this.setText(new Entry("", ""));
		curInfoShare.setRequestData(msg, -1);
	}

	private void showDialog(MyDatagram curMsg) {
		int op = curMsg.command;
		String key = curMsg.getKey();
		int state = curMsg.state;
		if (key == null) {
			JOptionPane.showMessageDialog(null, "Broken Massage", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		String opera = null;
		switch (op) {
		case Constants.SEARCH:
			opera = "search";
			break;
		case Constants.MODIFY:
			opera = "modify";
			break;
		case Constants.DELETE:
			opera = "delete";
			break;
		case Constants.INSERT:
			opera = "insert";
			break;
		case Constants.UPDATE:
			opera = "update";
			break;
		default:
			opera = "unknown";
			break;
		}
		String header = "The result of " + opera + " on " + key + " was ";
		String alreadyExist = ", because the word already exist";
		String noData = ", no such word found";
		String overWritten = ", however it has already been overwritten by other user";
		String title = null;
		int type = 0;
		switch (state) {
		case Constants.SUCCESS:
			header = header + "successful!";
			title = opera + " successful";
			break;
		case Constants.ENTRY_ALREADY_EXIST:
			header = header + "denyed" + alreadyExist;
			title = opera + " error";
			type = JOptionPane.ERROR_MESSAGE;
			break;
		case Constants.NO_SUCH_ENTRY:
			header = header + "denyed" + noData;
			title = opera + " error";
			type = JOptionPane.ERROR_MESSAGE;
			break;
		case Constants.OUTDATED_MODIFY:
			title = opera + " warning";
			header = header + "received" + overWritten;
			type = JOptionPane.ERROR_MESSAGE;
			break;
		default:
			title = "serious error";
			header = "unknown message";
			type = JOptionPane.WARNING_MESSAGE;
			break;
		}
		if (state == Constants.SUCCESS) {
			if (op != Constants.SEARCH)
				JOptionPane.showMessageDialog(null, header);
			else {
				header = header + ", and the explain is :" + curMsg.data.Explain;
			}
		} else
			JOptionPane.showMessageDialog(null, header, title, type);
	}

	private void writeBack(long reqNo) {
		//write the unsuccessful message back if its the last request made by user
		if (this.lastDatagram == null || this.lastDatagram.requestNumber != reqNo)
			return;
		Entry curText = this.getText(true);
		if (curText == null || (curText.Word.equals("") && curText.Explain.equals(""))) {
			if (this.lastDatagram.data.Explain != null) {
				this.lastDatagram.data.Explain = Entry.transformToRaw(this.lastDatagram.data.Explain);
			}
			this.setText(this.lastDatagram.data);
			this.lastDatagram = null;
		}
	};

	private void showMessageGeneral(MyDatagram curMsg) {

		if (curMsg.data.Explain != null) {
			curMsg.data.Explain = Entry.transformToRaw(curMsg.data.Explain);
		}

		if (curMsg.command == Constants.SEARCH && curMsg.state == Constants.SUCCESS) {
			Entry curText = this.getText(true);
			if (curText == null || (curText.Word.equals("") && curText.Explain.equals("")))
				this.setText(curMsg.data);
			else
				showDialog(curMsg);
		} else
			showDialog(curMsg);
		if (curMsg.state != 0)
			writeBack(curMsg.requestNumber);
		else
			this.lastDatagram = null;
		this.updateLabel(lblLastModify,
				UiStringTemplate.LAST_MODIFY.replaceAll("unavailable", this.convertDate(curMsg.getTime())));
	}

//***********************************************************************************************
	public void initGUI() {
		initParas();
		setLayouts();
		setListeners();
		setVisible(true);
		this.startDate = new Date();
	}

	private void initParas() {
		// Notice: for convenient, frame title was also initialized here.
		// init textStaff
		this.ftfSearchBox = new JFormattedTextField();
		textArea = new JTextArea();
		// init labels
		lblLastModify = new JLabel(UiStringTemplate.LAST_MODIFY);
		lblHeartBeat = new JLabel(UiStringTemplate.TIME_REMAIN);
		// init buttons
		JButton[] tmpBtns = { new JButton("Search"), new JButton("Modify"), new JButton("Delete"),
				new JButton("Insert"), new JButton("Update") };
		btns = tmpBtns;
		String titleString = UiStringTemplate.UI_HEADER;
		System.out.println("client" + String.valueOf(curInfoShare.clientNo) + " is starting, connecting port"
				+ String.valueOf(curInfoShare.port) + " at host " + curInfoShare.domain + "\n");
		titleString = titleString.replaceAll("undefined", String.valueOf(curInfoShare.clientNo));
		titleString = titleString.replaceAll("NoServer", curInfoShare.domain);
		titleString = titleString.replaceAll("NoPort", String.valueOf(curInfoShare.port));
		this.setTitle(titleString);
	}

	private void setLayouts() {
		//setting statical layouts
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		setResizable(false);

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0,
				Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.gridwidth = 2;
		gbc_formattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 0;
		gbc_formattedTextField.gridy = 0;
		contentPane.add(ftfSearchBox, gbc_formattedTextField);

		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.gridwidth = 2;
		gbc_textArea.gridheight = 9;
		gbc_textArea.insets = new Insets(0, 0, 5, 5);
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 0;
		gbc_textArea.gridy = 2;
		JScrollPane jsp = new JScrollPane(textArea);
		contentPane.add(jsp, gbc_textArea);

		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 11;
		contentPane.add(lblLastModify, gbc_lblNewLabel);

		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_3.gridx = 2;
		gbc_lblNewLabel_3.gridy = 9;
		contentPane.add(lblHeartBeat, gbc_lblNewLabel_3);

		GridBagConstraints gbc_btnSearch = new GridBagConstraints();
		gbc_btnSearch.insets = new Insets(0, 0, 5, 0);
		gbc_btnSearch.gridx = 2;
		gbc_btnSearch.gridy = 0;
		contentPane.add(btns[0], gbc_btnSearch);

		GridBagConstraints gbc_btnNewButton_3 = new GridBagConstraints();
		gbc_btnNewButton_3.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton_3.gridx = 2;
		gbc_btnNewButton_3.gridy = 2;
		contentPane.add(btns[1], gbc_btnNewButton_3);

		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.anchor = GridBagConstraints.NORTH;
		gbc_btnDelete.insets = new Insets(0, 0, 5, 0);
		gbc_btnDelete.gridx = 2;
		gbc_btnDelete.gridy = 3;
		contentPane.add(btns[2], gbc_btnDelete);

		GridBagConstraints gbc_btnNewButton_4 = new GridBagConstraints();
		gbc_btnNewButton_4.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton_4.gridx = 2;
		gbc_btnNewButton_4.gridy = 4;
		contentPane.add(btns[3], gbc_btnNewButton_4);

		GridBagConstraints gbc_btnNewButton_2 = new GridBagConstraints();
		gbc_btnNewButton_2.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton_2.anchor = GridBagConstraints.NORTH;
		gbc_btnNewButton_2.gridx = 2;
		gbc_btnNewButton_2.gridy = 5;
		contentPane.add(btns[4], gbc_btnNewButton_2);

		this.setContentPane(contentPane);
	}

}
