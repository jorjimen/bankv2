import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Date;

// GUI class that creates a frame from which the customer can pay back their loan
public class PayLoanFrame extends JFrame implements ActionListener {


    public static int FORM_WIDTH = 600;
    public static int FORM_Height = 400;

    private JPanel wholePanel;
    private JLabel choose_account_label;
    private JLabel choose_currency_label;
    private JLabel amount_label;
    private JButton backButton;
    private JButton enterButton;
    private JComboBox choose_account_cmb;
    private JComboBox choose_currency_cmb;
    private JLabel choose_payment;
    private JComboBox accountToPayFrom;
    private JTextField amount_text;
    private String amount_text_placeholder = "enter payment amount";
    private CustomerLoansFrame CustomerLoansFrame;
    private Customer customer;
    
    private ArrayList<Loan> loans = new ArrayList<Loan>();
    private String msg_1 = "You do not have any loan accounts";
    private String msg_2 = "Please create an account first";

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == backButton) {
            closeFrame();
            CustomerLoansFrame.setVisible(true);
        }
    }


    public PayLoanFrame(final CustomerLoansFrame CustomerLoansFrame , Customer customer){
        this.customer = customer;
        this.CustomerLoansFrame = CustomerLoansFrame;

        initUI();

        backButton.addActionListener(this);
        add(wholePanel, BorderLayout.CENTER);
        setTitle("Bank - Loan Payment" + " - " + Bank.date);
        setSize(FORM_WIDTH, FORM_Height);
        setVisible(true);

        initializeAccountComboBox(customer);

        amount_text.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (amount_text.getText().isEmpty()) {
                    amount_text.setForeground(Color.GRAY);
                    amount_text.setText(amount_text_placeholder);
                }
            }
        });

        amount_text.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (amount_text.getText().equals(amount_text_placeholder)) {
                    amount_text.setForeground(Color.BLACK);
                    amount_text.setText("");
                }
            }
        });

        enterButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (loans != null || loans.size() != 0 || choose_account_cmb.getSelectedItem().toString().length() != 26) {
                    String currency = choose_currency_cmb.getSelectedItem().toString();
                    String account = choose_account_cmb.getSelectedItem().toString();
                    String amount = amount_text.getText();

                    if(amount.equals("") || amount.equals(amount_text_placeholder)){
                        JOptionPane.showMessageDialog(rootPane, "Please enter the amount.");
                    } else {
                        Loan loan = null;
                        for (Loan loan_d : loans) {
                            if (loan_d.getID().toString().equals(account.split(":")[0])) {
                                loan = loan_d;
                                break;
                            }
                        }
                        switch (currency){
                            case "USD":
                                loan.makePayment(new Dollar(Double.valueOf(amount)));
                                break;
                            case "EUR":
                                loan.makePayment(new Euro(Double.valueOf(amount)));
                                break;
                            case "CNY":
                                loan.makePayment(new Yen(Double.valueOf(amount)));
                                break;
                        }

                        String account2 = accountToPayFrom.getSelectedItem().toString();

                        Customer c = getCustomer();
                        Account acc = c.getAccounts().get(c.findAccount(account2.split(":")[1]));
                        Withdrawl withdrawl = null;

                        switch (currency){
                            case "USD":
                                Dollar dollar = new Dollar(Double.valueOf(amount));
                                withdrawl = new Withdrawl(acc,c,dollar,Bank.date);
                                break;
                            case "EUR":
                                Euro euro = new Euro(Double.valueOf(amount));
                                withdrawl = new Withdrawl(acc,c,euro,Bank.date);
                                break;
                            case "CNY":
                                Yen yen = new Yen(Double.valueOf(amount));
                                withdrawl = new Withdrawl(acc,c,yen,Bank.date);
                                break;
                        }

                        acc.withdraw(withdrawl);
                        JOptionPane.showMessageDialog(rootPane, "Congratulations, your payment has been succesful!\n\n" + loan.toString() + "\n\nPAYMENT CONFIRMATION TO ACCOUNT " + acc.getID().toString() + ": " + withdrawl.toString());
                        closeFrame();
                        PersistanceHandler p = new PersistanceHandler();
                        p.saveState();
                        CustomerLoansFrame.setVisible(true);
                    }
                }
            }
        });
    }


    public void closeFrame() {
        this.dispose();
    }
    public Customer getCustomer() {
        return customer;
    }

    private boolean checking(){

        return false;
    }

    private void initializeAccountComboBox(Customer customer) {
        if (customer.getAccounts().size() == 0) {
            choose_account_cmb.addItem(msg_2);
        } else if (customer.getAllAccountsByType("loan").size() == 0) {
            choose_account_cmb.addItem(msg_1);
        } else {
            ArrayList<Account> allLoans = (customer.getAllAccountsByType("loan"));
            ArrayList<Loan> loans = new ArrayList<Loan>();
            for (Account loanAccount : allLoans) {
                loans.addAll(((LoanAccount) loanAccount).getLoans());
            }
            if (loans.size() == 0) {
                choose_account_cmb.addItem("You do not have any loans in your accounts. Request a Loan first."); 
            } else {
                for (int i = 0; i < loans.size(); i++) {
                    String accountNum = loans.get(i).getID().toString();
                    String accountType = loans.get(i).getRemainingBalance().toString();
                    choose_account_cmb.addItem(accountNum + ":" + accountType);
                }
            }
            this.loans = loans;
        }
        if (customer.getAccounts().size() == 0) {
            accountToPayFrom.addItem("Please create an account first");
        } else {
            for (int i = 0; i < customer.getAccounts().size(); i++) {
                String accountNum = customer.getAccounts().get(i).getID().toString();
                String accountType = customer.getAccounts().get(i).getAccountType();
                String balance = customer.getAccounts().get(i).getAmount().toString();
                accountToPayFrom.addItem(accountType + ":" + accountNum + ":" + balance);
            }
        }
    }


    public void initUI(){
        wholePanel =  new JPanel();
        wholePanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        wholePanel.setBackground(new Color(-524801));

        final JPanel panel1 = new JPanel();
        panel1.setBackground(new Color(-524801));
        wholePanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        final JLabel label1 = new JLabel();
        label1.setBackground(new Color(-524801));
        label1.setForeground(new Color(-16777216));
        label1.setText("Process Payment");
        panel1.add(label1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(4, 8, new Insets(0, 0, 0, 0), -1, -1));
        panel2.setBackground(new Color(-524801));
        wholePanel.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        choose_currency_label = new JLabel();
        choose_currency_label.setBackground(new Color(-524801));
        choose_currency_label.setForeground(new Color(-16777216));
        choose_currency_label.setText("Currency:");
        panel2.add(choose_currency_label, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        choose_currency_cmb =  new JComboBox();
        choose_currency_cmb.addItem("USD");
        choose_currency_cmb.addItem("CNY");
        choose_currency_cmb.addItem("EUR");
        panel2.add(choose_currency_cmb, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        choose_account_label = new JLabel();
        choose_account_label.setBackground(new Color(-524801));
        choose_account_label.setForeground(new Color(-16777216));
        choose_account_label.setText("Choose Loan:");
        panel2.add(choose_account_label, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        choose_account_cmb =  new JComboBox();
        panel2.add(choose_account_cmb, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        choose_payment = new JLabel();
        choose_payment.setBackground(new Color(-524801));
        choose_payment.setForeground(new Color(-16777216));
        choose_payment.setText("Choose Payment Option:");
        panel2.add(choose_payment, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        accountToPayFrom = new JComboBox();
        panel2.add(accountToPayFrom, new GridConstraints(2, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        
        amount_label = new JLabel();
        amount_label.setBackground(new Color(-524801));
        amount_label.setForeground(new Color(-16777216));
        amount_label.setText("Amount:");
        panel2.add(amount_label, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        amount_text = new JTextField();
        amount_text.setBackground(new Color(-1118482));
        amount_text.setForeground(new Color(-4473925));
        amount_text.setText(amount_text_placeholder);
        panel2.add(amount_text, new GridConstraints(3, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(30, -1), null, 0, false));


        final JPanel panel3 = new JPanel();
        panel3.setBackground(new Color(-524801));
        wholePanel.add(panel3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        backButton = new JButton();
        backButton.setBackground(new Color(-1118482));
        backButton.setForeground(new Color(-16777216));
        backButton.setText("Back");
        panel3.add(backButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, -1), null, 0, false));

        enterButton = new JButton();
        enterButton.setBackground(new Color(-1118482));
        enterButton.setForeground(new Color(-16777216));
        enterButton.setText("Enter");
        panel3.add(enterButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, -1), null, 0, false));

    }
 
}
