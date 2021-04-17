/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf.managedbean;

import ejb.session.stateless.ProductEntitySessionBeanLocal;
import ejb.session.stateless.SaleTransactionEntitySessionBeanLocal;
import entity.CustomerEntity;
import entity.ProductEntity;
import entity.SaleTransactionEntity;
import entity.SaleTransactionLineItemEntity;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import static oracle.jrockit.jfr.events.Bits.intValue;
import util.exception.CreateNewSaleTransactionException;
import util.exception.CustomerNotFoundException;

/**
 *
 * @author User
 */
@Named(value = "shoppingCartManagedBean")
@SessionScoped
public class ShoppingCartManagedBean implements Serializable {

    @EJB
    private ProductEntitySessionBeanLocal productEntitySessionBeanLocal;

    @EJB
    private SaleTransactionEntitySessionBeanLocal saleTransactionEntitySessionBeanLocal;

    private List<ProductEntity> allProducts;
    private List<SaleTransactionLineItemEntity> saleTransactionLineItemEntities;
    private List<SaleTransactionLineItemEntity> lineItemsToBuy;

    private BigDecimal totalAmount;
    private Integer totalLineItem;
    private Integer totalQuantity;

    private CustomerEntity currentCustomer;

    public ShoppingCartManagedBean() {
        initialiseState();
    }

    @PostConstruct
    public void postConstruct() {
        allProducts = productEntitySessionBeanLocal.retrieveAllProducts();
        for (ProductEntity product : allProducts) {
            SaleTransactionLineItemEntity lineItem = new SaleTransactionLineItemEntity(0, product, 0, product.getUnitPrice(), totalAmount);
            saleTransactionLineItemEntities.add(lineItem);
        }
    }

    private void initialiseState() {
        saleTransactionLineItemEntities = new ArrayList<>();
        lineItemsToBuy = new ArrayList<>();
        totalLineItem = 0;
        totalQuantity = 0;
        totalAmount = new BigDecimal("0.00");
    }

    public void addLineItem(ActionEvent event) {
        SaleTransactionLineItemEntity lineItem = (SaleTransactionLineItemEntity) event.getComponent().getAttributes().get("lineItemToBuy");

        if (lineItem.getSerialNumber() == 0) {
            // line Item not added to shopping cart yet
            lineItem.setSerialNumber(++totalLineItem);
            totalQuantity += lineItem.getQuantity();
            BigDecimal subTotal = lineItem.getUnitPrice().multiply(new BigDecimal(lineItem.getQuantity()));
            lineItem.setSubTotal(subTotal);
            totalAmount = totalAmount.add(subTotal);
            lineItemsToBuy.add(lineItem);

        } else {
            // line item alrdy added to shopping cart
            BigDecimal prevQuantity = lineItem.getSubTotal().divide(lineItem.getUnitPrice());
            totalQuantity -= intValue(prevQuantity);
            totalAmount = totalAmount.subtract(lineItem.getSubTotal());

            totalQuantity += lineItem.getQuantity();
            BigDecimal subTotal = lineItem.getUnitPrice().multiply(new BigDecimal(lineItem.getQuantity()));
            lineItem.setSubTotal(subTotal);
            totalAmount = totalAmount.add(subTotal);
        }

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Item added to shopping card !", null));
    }

    public void updateQuantityForShoppingCartItem(ActionEvent event) {
        SaleTransactionLineItemEntity lineItemToUpdate = (SaleTransactionLineItemEntity) event.getComponent().getAttributes().get("lineItemToUpdate");

        BigDecimal prevQuantity = lineItemToUpdate.getSubTotal().divide(lineItemToUpdate.getUnitPrice());
        totalQuantity -= intValue(prevQuantity);
        totalAmount = totalAmount.subtract(lineItemToUpdate.getSubTotal());

        if (lineItemToUpdate.getQuantity() > 0) {
            totalQuantity += lineItemToUpdate.getQuantity();
            BigDecimal subTotal = lineItemToUpdate.getUnitPrice().multiply(new BigDecimal(lineItemToUpdate.getQuantity()));
            lineItemToUpdate.setSubTotal(subTotal);
            totalAmount = totalAmount.add(subTotal);
            lineItemsToBuy.set(0, lineItemToUpdate);
        } else {
            lineItemsToBuy.remove(lineItemToUpdate);
        }
    }

    public void doCheckoutForCustomer(ActionEvent event) throws CustomerNotFoundException, CreateNewSaleTransactionException {
        currentCustomer = (CustomerEntity) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("currentCustomer");
        SaleTransactionEntity newSaleTransactionEntity = saleTransactionEntitySessionBeanLocal.createNewSaleTransactionForCustomer(getCurrentCustomer().getCustomerId(), new SaleTransactionEntity(getTotalLineItem(), getTotalQuantity(), getTotalAmount(), new Date(), getLineItemsToBuy(), false));
        clearShoppingCart();

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "You have successfully checkout ! (Checkout ID: " + newSaleTransactionEntity.getSaleTransactionId() + ")", null));
    }

    public void clearShoppingCart() {
        initialiseState();
        // reset lineItemEntities
        allProducts = productEntitySessionBeanLocal.retrieveAllProducts();
        for (ProductEntity product : allProducts) {
            SaleTransactionLineItemEntity lineItem = new SaleTransactionLineItemEntity(0, product, 0, product.getUnitPrice(), totalAmount);
            saleTransactionLineItemEntities.add(lineItem);
        }
    }

    /**
     * @return the saleTransactionLineItemEntities
     */
    public List<SaleTransactionLineItemEntity> getSaleTransactionLineItemEntities() {
        return saleTransactionLineItemEntities;
    }

    /**
     * @param saleTransactionLineItemEntities the
     * saleTransactionLineItemEntities to set
     */
    public void setSaleTransactionLineItemEntities(List<SaleTransactionLineItemEntity> saleTransactionLineItemEntities) {
        this.saleTransactionLineItemEntities = saleTransactionLineItemEntities;
    }

    /**
     * @return the totalAmount
     */
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    /**
     * @param totalAmount the totalAmount to set
     */
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    /**
     * @return the totalLineItem
     */
    public Integer getTotalLineItem() {
        return totalLineItem;
    }

    /**
     * @param totalLineItem the totalLineItem to set
     */
    public void setTotalLineItem(Integer totalLineItem) {
        this.totalLineItem = totalLineItem;
    }

    /**
     * @return the totalQuantity
     */
    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    /**
     * @param totalQuantity the totalQuantity to set
     */
    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    /**
     * @return the currentCustomer
     */
    public CustomerEntity getCurrentCustomer() {
        return currentCustomer;
    }

    /**
     * @param currentCustomer the currentCustomer to set
     */
    public void setCurrentCustomer(CustomerEntity currentCustomer) {
        this.currentCustomer = currentCustomer;
    }

    /**
     * @return the lineItemsToBuy
     */
    public List<SaleTransactionLineItemEntity> getLineItemsToBuy() {
        return lineItemsToBuy;
    }

    /**
     * @param lineItemsToBuy the lineItemsToBuy to set
     */
    public void setLineItemsToBuy(List<SaleTransactionLineItemEntity> lineItemsToBuy) {
        this.lineItemsToBuy = lineItemsToBuy;
    }

    /**
     * @return the allProducts
     */
    public List<ProductEntity> getAllProducts() {
        return allProducts;
    }

    /**
     * @param allProducts the allProducts to set
     */
    public void setAllProducts(List<ProductEntity> allProducts) {
        this.allProducts = allProducts;
    }

}
