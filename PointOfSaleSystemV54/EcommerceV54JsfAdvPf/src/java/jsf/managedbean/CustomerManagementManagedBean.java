/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf.managedbean;

import ejb.session.stateless.CustomerEntitySessionBeanLocal;
import entity.CustomerEntity;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import util.exception.CustomerEmailExistException;
import util.exception.InputDataValidationException;
import util.exception.UnknownPersistenceException;

/**
 *
 * @author User
 */
@Named(value = "customerManagementManagedBean")
@ViewScoped
public class CustomerManagementManagedBean implements Serializable {

    @EJB(name = "CustomerEntitySessionBeanLocal")
    private CustomerEntitySessionBeanLocal customerEntitySessionBeanLocal;

    private CustomerEntity customer;

    public CustomerManagementManagedBean() {
        customer = new CustomerEntity();
    }

    @PostConstruct
    public void postConstruct() {
    }

    public void registerNewCustomer(ActionEvent event) {
        try {
            setCustomer(customerEntitySessionBeanLocal.createNewCustomer(getCustomer()));

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "New customer created successfully (Customer ID: " + getCustomer().getCustomerId() + ")", null));

            setCustomer(new CustomerEntity());

        } catch (InputDataValidationException | UnknownPersistenceException | CustomerEmailExistException ex) {

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "An error has occurred while creating the new customer: " + ex.getMessage(), null));
        }
    }

    /**
     * @return the customer
     */
    public CustomerEntity getCustomer() {
        return customer;
    }

    /**
     * @param customer the customer to set
     */
    public void setCustomer(CustomerEntity customer) {
        this.customer = customer;
    }

}
