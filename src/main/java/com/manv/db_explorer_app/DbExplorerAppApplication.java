package com.manv.db_explorer_app;

import com.manv.db_explorer_app.gui.DatabaseGUI;
import com.manv.db_explorer_app.service.DatabaseService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;
import java.awt.*;

@SpringBootApplication
public class DbExplorerAppApplication {

	public static void main(String[] args) {
		var context = SpringApplication.run(DbExplorerAppApplication.class, args);
		DatabaseService databaseService = context.getBean(DatabaseService.class);

		// Запуск GUI
		SwingUtilities.invokeLater(() -> {
			DatabaseGUI gui = new DatabaseGUI(databaseService);
			gui.setVisible(true);
		});

		//headless mode check, if true change run options and add VM "-Djava.awt.headless=false"
//		boolean isHeadless = GraphicsEnvironment.isHeadless();
//		System.out.println("Is headless mode enabled? " + isHeadless);
	}

}
