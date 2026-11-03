
package com.my.rem;

import java.util.ArrayList;

    public class Client {
    private int clientId;
    private String name;
    private String contactInfo;
    private ArrayList<Integer> propertiesOwned;

    public Client(int clientId, String name, String contactInfo) {
        this.clientId = clientId;
        this.name = name;
        this.contactInfo = contactInfo;
        this.propertiesOwned = new ArrayList<>();
    }

    public int getClientId() { return clientId; }
    public String getName() { return name; }
    public String getContactInfo() { return contactInfo; }
    public ArrayList<Integer> getPropertiesOwned() { return propertiesOwned; }

    public void setName(String name) { this.name = name; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public void addOwnedProperty(int propertyId) {
        propertiesOwned.add(propertyId);
    }

    public String getOwnedAsText() {
        if (propertiesOwned.isEmpty()) return "None";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < propertiesOwned.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(propertiesOwned.get(i));
        }
        return sb.toString();
    }
}
