/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */

package truelauncher.launcher;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import truelauncher.client.ClientLaunch;
import truelauncher.client.ClientUpdateThread;
import truelauncher.client.ClientUpdateThread.ClientDownloadFailedEvent;
import truelauncher.client.ClientUpdateThread.ClientDownloadFinishedEvent;
import truelauncher.client.ClientUpdateThread.ClientDownloadRunningEvent;
import truelauncher.client.ClientUpdateThread.ClientDownloadStageChangeEvent;
import truelauncher.client.ClientUpdateThread.ClientDownloadStartedEvent;
import truelauncher.client.ClientUpdateThread.ClientUnzipRunningEvent;
import truelauncher.client.ClientUpdateThread.ClientUnzipStartedEvent;
import truelauncher.config.AllSettings;
import truelauncher.events.EventBus;
import truelauncher.events.EventBus.EventHandler;
import truelauncher.gcomponents.TButton;
import truelauncher.gcomponents.TComboBox;
import truelauncher.gcomponents.TLabel;
import truelauncher.gcomponents.TPasswordField;
import truelauncher.gcomponents.TProgressBar;
import truelauncher.gcomponents.TTextField;
import truelauncher.images.Images;
import truelauncher.userprefs.fields.UserFieldsChoice;
import truelauncher.userprefs.settings.UserLauncherSettings;
import truelauncher.utils.LauncherUtils;

@SuppressWarnings("serial")
public class GUI extends JPanel {

	private static GUI staticgui;

	private TTextField nicknameField;
	private TPasswordField passwordField;
	private TComboBox launchClientListCombobox;
	private TButton launchClientButton;
	private TProgressBar downloadClientProgressbar;
	private TButton downloadClientButton;
	private TComboBox downloadClientListCombobox;
	private LauncherUpdateDialog launcherUpdateDialog;
	private LauncherSettingsDialog launcherSettingsDialog;
	private JFrame frame;

	private boolean guiinitfinished = false;
	public GUI(JFrame frame) {
		try {
			staticgui = this;
			//load settings
			AllSettings.load();
			//load user prefs data
			UserLauncherSettings.loadConfig();
			UserFieldsChoice.loadConfig();
			//create gui
			this.frame = frame;
			setLayout(null);
			setBorder(BorderFactory.createBevelBorder(1, Color.GRAY, Color.GRAY));
			initUI();
			// load comboboxes values
			fillClients();
			// load client fields values
			loadPrefs();
			// register listener
			EventBus.registerListener(this);
			// gui init and settings load finished
			guiinitfinished = true;
		} catch (Exception e) {
			LauncherUtils.logError(e);
		}
	}

	private void initUI() {
		initHeader();
		initSettingsButton();
		initCloseMinimizeButton();
		initTextInputFieldsAndLabels();
		initStartButton();
		initDownloadCenter();
		initLauncherUpdater();
		initLauncherSettings();
	}

	// header
	private int posX = 0, posY = 0;

	private void initHeader() {
		JLabel drag = new JLabel();
		drag.setBounds(0, 0, GUISettings.w, 20);
		drag.setOpaque(false);
		drag.setBackground(new Color(0, 0, 0, 0));
		drag.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				posX = e.getX();
				posY = e.getY();
			}
		});
		drag.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent evt) {
				frame.setLocation(evt.getXOnScreen() - posX, evt.getYOnScreen() - posY);
			}
		});
		this.add(drag);
	}

	// settings button
	private void initSettingsButton() {
		JPanel sb = new JPanel();
		sb.setLayout(null);
		sb.setBounds(20, 20, 25, 25);
		sb.setOpaque(false);
		sb.setBackground(new Color(0, 0, 0, 0));

		TButton settings = new TButton();
		settings.setBounds(0, 0, 25, 25);
		settings.setOpaque(false);
		settings.setBackground(new Color(0, 0, 0, 0));
		settings.setBackgroundImage(
			Images.class.getResourceAsStream(GUISettings.options),
			Images.class.getResourceAsStream("pr_"+GUISettings.options),
			Images.class.getResourceAsStream("f_"+GUISettings.options)
		);
		settings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openSettingsWindow();
			}
		});
		sb.add(settings);

		this.add(sb);
	}

	// close and minimize buttonis block
	private void initCloseMinimizeButton() {
		JPanel cmb = new JPanel();
		cmb.setLayout(null);
		cmb.setBounds(GUISettings.w - 75, 20, 60, 25);
		cmb.setOpaque(false);
		cmb.setBackground(new Color(0, 0, 0, 0));

		TButton minimize = new TButton();
		minimize.setBounds(0, 0, 25, 25);
		minimize.setOpaque(false);
		minimize.setBackground(new Color(0, 0, 0, 0));
		minimize.setBackgroundImage(
			Images.class.getResourceAsStream(GUISettings.hide),
			Images.class.getResourceAsStream("pr_"+GUISettings.hide),
			Images.class.getResourceAsStream("f_"+GUISettings.hide)
		);
		minimize.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setExtendedState(frame.getExtendedState() | Frame.ICONIFIED);
			}
		});
		cmb.add(minimize);

		TButton close = new TButton();
		close.setBounds(35, 0, 25, 25);
		close.setOpaque(false);
		close.setBackground(new Color(0, 0, 0, 0));
		close.setBackgroundImage(
			Images.class.getResourceAsStream(GUISettings.close),
			Images.class.getResourceAsStream("pr_"+GUISettings.close),
			Images.class.getResourceAsStream("f_"+GUISettings.close)
		);
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		cmb.add(close);

		this.add(cmb);
	}

	// block 1 (nickname chooser)
	private void initTextInputFieldsAndLabels() {
		int y = GUISettings.h - 110;
		int levelw = 30;
		int widgw = 220;
		JPanel tifields = new JPanel();
		tifields.setLayout(null);
		tifields.setBounds(levelw, y, widgw, 95);
		tifields.setOpaque(false);
		tifields.setBackground(new Color(0, 0, 0, 0));

		// Плашка объясениния
		TLabel expbarset = new TLabel();
		expbarset.setBounds(0, 0, widgw, 25);
		expbarset.setBackgroundImage(Images.class.getResourceAsStream(GUISettings.explainimage));
		expbarset.setText("Основные настройки");
		expbarset.setHorizontalAlignment(SwingConstants.CENTER);
		tifields.add(expbarset);

		// Плашка ника
		int lnw = 80;
		int lnh = 20;
		TLabel labelnick = new TLabel();
		labelnick.setBounds(0, 25, lnw, lnh);
		labelnick.setBackgroundImage(Images.class.getResourceAsStream(GUISettings.labelimage));
		labelnick.setText("Ник");
		labelnick.setHorizontalAlignment(SwingConstants.CENTER);
		tifields.add(labelnick);
		// Поле ника
		int inw = 140;
		nicknameField = new TTextField();
		nicknameField.setBounds(lnw, 25, inw, lnh);
		nicknameField.setText("NoNickName");
		nicknameField.setHorizontalAlignment(SwingConstants.CENTER);
		tifields.add(nicknameField);

		// Плашка пароля
		int lrw = 80;
		int lrh = 20;
		TLabel labelpass = new TLabel();
		labelpass.setBounds(0, 45, lrw, lrh);
		labelpass.setText("Пароль");
		labelpass.setHorizontalAlignment(SwingConstants.CENTER);
		labelpass.setBackgroundImage(Images.class.getResourceAsStream(GUISettings.labelimage));
		tifields.add(labelpass);
		// Поле пароля
		int irw = 140;
		passwordField = new TPasswordField();
		passwordField.setBounds(lrw, 45, irw, lrh);
		passwordField.setText("");
		passwordField.setHorizontalAlignment(SwingConstants.CENTER);
		tifields.add(passwordField);

		// Кнопка сохранить
		TButton save = new TButton();
		save.setText("Сохранить настройки");
		save.setBounds(0, 65, widgw, 30);
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UserFieldsChoice.nick = staticgui.nicknameField.getText();
				UserFieldsChoice.password = new String(staticgui.passwordField.getPassword());
				UserFieldsChoice.saveBlock1Config();
			}
		});
		tifields.add(save);

		this.add(tifields);
	}

	// block 2 (clients start)
	private void initStartButton() {
		int y = GUISettings.h - 110;
		int levelw = 250;
		int widgw = 240;

		JPanel sb = new JPanel();
		sb.setLayout(null);
		sb.setBounds(levelw, y, widgw, 95);
		sb.setOpaque(false);
		sb.setBackground(new Color(0, 0, 0, 0));

		// плашка объяснений
		TLabel expbarset = new TLabel();
		expbarset.setBounds(0, 0, widgw, 25);
		expbarset.setText("Выбор клиента");
		expbarset.setHorizontalAlignment(SwingConstants.CENTER);
		expbarset.setBackgroundImage(Images.class.getResourceAsStream(GUISettings.explainimage));
		sb.add(expbarset);

		launchClientListCombobox = new TComboBox();
		launchClientListCombobox.setBounds(0, 25, widgw, 30);
		launchClientListCombobox.setAlignmentY(Component.CENTER_ALIGNMENT);
		launchClientListCombobox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (guiinitfinished && (launchClientListCombobox.getItemCount() > 0)) {
					checkClientInternal(launchClientListCombobox.getSelectedItem().toString());
					UserFieldsChoice.launchclient = launchClientListCombobox.getSelectedItem().toString();
					UserFieldsChoice.saveBlock23Config();
				}
			}
		});
		sb.add(launchClientListCombobox);

		// кнопка запуска майна
		launchClientButton = new TButton();
		launchClientButton.setBounds(0, 55, widgw, 40);
		launchClientButton.setText("Запустить Minercraft");
		launchClientButton.setEnabled(false);
		launchClientButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// selection name
				String clientname = launchClientListCombobox.getSelectedItem().toString();
				// laucnher folder
				String mcpath = LauncherUtils.getDir() + File.separator + AllSettings.getClientFolderByName(clientname);
				// nickname
				String nick = nicknameField.getText();
				// RAM
				int ram = 512;
				boolean amd64 = System.getProperty("sun.arch.data.model").contains("64");
				if (amd64) {
					ram = 1024;
				}
				if (UserLauncherSettings.memory > 0) {
					if (UserLauncherSettings.memory < 256) {
						ram = 256;
					} else {
						if (!amd64) {
							if (UserLauncherSettings.memory > 1024) {
								ram = 1024;
							}
						} else {
							ram = UserLauncherSettings.memory;
						}
					}
				}
				String mem = Integer.valueOf(ram) + "M";
				// password
				String password = new String(passwordField.getPassword());
				// location of jar file
				String jar = LauncherUtils.getDir() + File.separator + AllSettings.getClientJarByName(clientname);
				// mainclass
				String mainclass = AllSettings.getClientMainClassByName(clientname);
				// cmdargs
				String cmdargs = AllSettings.getClientCmdArgsByName(clientname);
				// launch minecraft (mcpach, nick, mem, jar, mainclass, cmdargs)
				ClientLaunch.launchMC(mcpath, nick, password, mem, jar, mainclass, cmdargs);
			}
		});
		sb.add(launchClientButton);

		this.add(sb);
	}

	// block 3 (clients download)
	private void initDownloadCenter() {
		int y = GUISettings.h - 110;
		int levelw = 490;
		int widgw = 220;

		JPanel dc = new JPanel();
		dc.setLayout(null);
		dc.setBounds(levelw, y, widgw, 95);
		dc.setOpaque(false);
		dc.setBackground(new Color(0, 0, 0, 0));

		TLabel expbarset = new TLabel();
		expbarset.setBounds(0, 0, widgw, 25);
		expbarset.setBackgroundImage(Images.class.getResourceAsStream(GUISettings.explainimage));
		expbarset.setText("Скачивание клиентов");
		expbarset.setHorizontalAlignment(SwingConstants.CENTER);
		dc.add(expbarset);

		downloadClientListCombobox = new TComboBox();
		downloadClientListCombobox.setBounds(0, 25, widgw, 30);
		downloadClientListCombobox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (guiinitfinished && (downloadClientListCombobox.getItemCount() > 0)) {
					downloadClientButton.setText("Скачать клиент");
					downloadClientProgressbar.setValue(0);
					downloadClientButton.setEnabled(true);
					UserFieldsChoice.downloadclient = downloadClientListCombobox.getSelectedItem().toString();
					UserFieldsChoice.saveBlock23Config();
				}
			}
		});
		dc.add(downloadClientListCombobox);

		downloadClientProgressbar = new TProgressBar();
		downloadClientProgressbar.setBounds(0, 55, widgw, 16);
		dc.add(downloadClientProgressbar);

		downloadClientButton = new TButton();
		downloadClientButton.setBounds(0, 70, widgw, 25);
		downloadClientButton.setText("Скачать клиент");
		downloadClientButton.setHorizontalAlignment(SwingConstants.CENTER);
		downloadClientButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// lock gui
				downloadClientListCombobox.setEnabled(false);
				downloadClientButton.setEnabled(false);
				// get client name
				String client = downloadClientListCombobox.getSelectedItem().toString();
				// run client update
				new ClientUpdateThread(client).start();
			}
		});
		dc.add(downloadClientButton);

		this.add(dc);
	}

	// Init laucnher updater
	private void initLauncherUpdater() {
		launcherUpdateDialog = new LauncherUpdateDialog();
		new LauncherVersionChecker().start();
	}

	// Init Launcher settings
	private void initLauncherSettings() {
		launcherSettingsDialog = new LauncherSettingsDialog();
	}


	// Some methods
	// Fill clients comboboxes
	private void fillClients() {
		for (String servname : AllSettings.getClientsList()) {
			if (AllSettings.getClientRemovedStatusByName(servname)) {
				File cfile = new File(LauncherUtils.getDir() + File.separator + AllSettings.getClientJarByName(servname));
				if (!cfile.exists()) {
					continue;
				}
			}
			launchClientListCombobox.addItem(servname);
		}
		for (String servname : AllSettings.getClientsList()) {
			if (!AllSettings.getClientRemovedStatusByName(servname)) {
				downloadClientListCombobox.addItem(servname);
			}
		}
		if (launchClientListCombobox.getItemCount() > 0) {
			checkClientInternal(launchClientListCombobox.getSelectedItem().toString());
		}
	}

	//load client prefs
	private void loadPrefs() {
		nicknameField.setText(UserFieldsChoice.nick);
		passwordField.setText(UserFieldsChoice.password);
		for (int i = 0; i < launchClientListCombobox.getItemCount(); i++) {
			String clientname = launchClientListCombobox.getItemAt(i);
			if (clientname.equals(UserFieldsChoice.launchclient)) {
				launchClientListCombobox.setSelectedIndex(i);
				break;
			}
		}
		for (int i = 0; i < downloadClientListCombobox.getItemCount(); i++) {
			String clientname = downloadClientListCombobox.getItemAt(i);
			if (clientname.equals(UserFieldsChoice.downloadclient)) {
				downloadClientListCombobox.setSelectedIndex(i);
				break;
			}
		}
		if (launchClientListCombobox.getItemCount() > 0) {
			checkClientInternal(launchClientListCombobox.getSelectedItem().toString());
		}
	}

	// check client jar and version
	private void checkClientInternal(String client) {
		File cfile = new File(LauncherUtils.getDir() + File.separator + AllSettings.getClientJarByName(client));
		File versionfile = new File(LauncherUtils.getDir() + File.separator + AllSettings.getClientFolderByName(client) + File.separator + "clientversion");
		// first check the jar
		if (cfile.exists()) {
			if (UserLauncherSettings.updateclient) {
				// now check the version
				try {
					Scanner scan = new Scanner(versionfile);
					int currentversion = scan.nextInt();
					scan.close();
					if (currentversion < AllSettings.getClientVersionByName(client)) {
						launchClientButton.setEnabled(false);
						launchClientButton.setText("Требуется обновление");
					} else {
						launchClientButton.setEnabled(true);
						launchClientButton.setText("✔ Запустить Minecraft");
					}
				} catch (Exception e) {
					LauncherUtils.logError(e);
					launchClientButton.setEnabled(true);
					launchClientButton.setText("✘ Запустить Minecraft");
				}
			} else {
				launchClientButton.setEnabled(true);
				launchClientButton.setText("✔ Запустить Minecraft");
			}
		} else {
			launchClientButton.setText("☠ Клиент не найден");
			launchClientButton.setEnabled(false);
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		try {
			Image bg = ImageIO.read(Images.class.getResourceAsStream(GUISettings.bgimage));
			bg = bg.getScaledInstance(GUISettings.w, GUISettings.h, Image.SCALE_SMOOTH);
			g.drawImage(bg, 0, 0, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// static access

	//check if gui is ready
	public static boolean isGUIReady() {
		return staticgui.guiinitfinished;
	}

	// open launcher update window
	public static void openUpdateWindow() {
		while (!GUI.isGUIReady()) {
			try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
		}
		staticgui.frame.getGlassPane().setVisible(true);
		staticgui.launcherUpdateDialog.open(staticgui);
	}

	// close launcher update window
	public static void closeUpdateWindow() {
		staticgui.launcherUpdateDialog.dispose();
		staticgui.frame.getGlassPane().setVisible(false);
	}

	// open settings window
	public static void openSettingsWindow() {
		while (!GUI.isGUIReady()) {
			try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
		}
		staticgui.frame.getGlassPane().setVisible(true);
		staticgui.launcherSettingsDialog.open(staticgui);
	}

	// close settings window
	public static void closeSettingsWindow() {
		staticgui.launcherSettingsDialog.dispose();
		staticgui.frame.getGlassPane().setVisible(false);
	}

	// reinit client comboboxes
	public static void refreshClients() {
		while (!GUI.isGUIReady()) {
			try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
		}
		staticgui.launchClientListCombobox.removeAllItems();
		staticgui.downloadClientListCombobox.removeAllItems();
		staticgui.fillClients();
	}

	// event listeners

	@EventHandler
	public void onClientDownloadStarted(ClientDownloadStartedEvent event) {
		downloadClientProgressbar.setMinimum(0);
		downloadClientProgressbar.setMaximum((int) event.getClientFileSize());
		downloadClientProgressbar.setValue(0);
	}

	@EventHandler
	public void onClientDownloadRunning(ClientDownloadRunningEvent event) {
		downloadClientProgressbar.setValue((int) event.getDownloadedAmount());
	}

	@EventHandler
	public void onClientDownloadStageChanged(ClientDownloadStageChangeEvent event) {
		downloadClientButton.setText(event.getStage());
	}

	@EventHandler
	public void onClientDownloadFinished(ClientDownloadFinishedEvent event) {
		downloadClientButton.setText("Клиент установлен");
		downloadClientListCombobox.setEnabled(true);
		checkClientInternal(event.getClient());
	}

	@EventHandler
	public void onClientDownloadFailed(ClientDownloadFailedEvent event) {
		downloadClientButton.setText("Ошибка");
		downloadClientListCombobox.setEnabled(true);
	}

	@EventHandler
	public void onClientUnzipStarted(ClientUnzipStartedEvent event) {
		downloadClientProgressbar.setMinimum(0);
		downloadClientProgressbar.setMaximum((int) event.getClientFilesCount());
		downloadClientProgressbar.setValue(0);
	}

	@EventHandler
	public void onClientUnzipRunning(ClientUnzipRunningEvent event) {
		downloadClientProgressbar.setValue((int) event.getUnpackedAmount());
	}

}