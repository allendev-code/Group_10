
package com.my.rem;


    public class Transaction {
    private int transactionId;
    private int propertyId;
    private int clientId;
    private String date;
    private double amount;
    private String recordedBy;

    public Transaction(int transactionId, int propertyId, int clientId, String date, double amount, String recordedBy) {
        this.transactionId = transactionId;
        this.propertyId = propertyId;
        this.clientId = clientId;
        this.date = date;
        this.amount = amount;
        this.recordedBy = recordedBy;
    }

    public int getTransactionId() { return transactionId; }
    public int getPropertyId() { return propertyId; }
    public int getClientId() { return clientId; }
    public String getDate() { return date; }
    public double getAmount() { return amount; }
    public String getRecordedBy() { return recordedBy; }
}

