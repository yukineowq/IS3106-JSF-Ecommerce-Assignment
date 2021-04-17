package ejb.session.stateless;

import entity.CustomerEntity;
import javax.ejb.Local;
import util.exception.CustomerEmailExistException;
import util.exception.CustomerNotFoundException;
import util.exception.InputDataValidationException;
import util.exception.InvalidLoginCredentialException;
import util.exception.UnknownPersistenceException;

@Local

public interface CustomerEntitySessionBeanLocal {

    public CustomerEntity retrieveCustomerByEmail(String email) throws CustomerNotFoundException;

    public CustomerEntity customerLogin(String email, String password) throws InvalidLoginCredentialException;

    public CustomerEntity createNewCustomer(CustomerEntity newCustomerEntity) throws CustomerEmailExistException, UnknownPersistenceException, InputDataValidationException;

    public CustomerEntity retrieveCustomerById(Long id) throws CustomerNotFoundException;

}
