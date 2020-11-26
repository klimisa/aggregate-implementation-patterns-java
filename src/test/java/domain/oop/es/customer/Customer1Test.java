package domain.oop.es.customer;

import domain.THelper;
import domain.shared.command.ChangeCustomerEmailAddress;
import domain.shared.command.ConfirmCustomerEmailAddress;
import domain.shared.command.RegisterCustomer;
import domain.shared.event.CustomerEmailAddressChanged;
import domain.shared.event.CustomerEmailAddressConfirmationFailed;
import domain.shared.event.CustomerEmailAddressConfirmed;
import domain.shared.event.CustomerRegistered;
import domain.shared.value.EmailAddress;
import domain.shared.value.Hash;
import domain.shared.value.ID;
import domain.shared.value.PersonName;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class Customer1Test {
    private ID customerID;
    private EmailAddress emailAddress;
    private EmailAddress changedEmailAddress;
    private Hash confirmationHash;
    private Hash wrongConfirmationHash;
    private Hash changedConfirmationHash;
    private PersonName name;
    private Customer1 registeredCustomer;

    @BeforeEach
    void beforeEach() {
        customerID = ID.generate();
        emailAddress = EmailAddress.build("john@doe.com");
        changedEmailAddress = EmailAddress.build("john+changed@doe.com");
        confirmationHash = Hash.generate();
        wrongConfirmationHash = Hash.generate();
        changedConfirmationHash = Hash.generate();
        name = PersonName.build("John", "Doe");
    }

    @Test
    @Order(1)
    void registerCustomer() {
        WHEN_RegisterCustomer();
        THEN_CustomerRegistered();
    }

    @Test
    @Order(2)
    void confirmEmailAddress() {
        GIVEN_CustomerRegistered();
        WHEN_ConfirmCustomerEmailAddress_With(confirmationHash);
        THEN_CustomerEmailAddressConfirmed();
    }

    @Test
    @Order(3)
    void confirmEmailAddress_withWrongConfirmationHash() {
        GIVEN_CustomerRegistered();
        WHEN_ConfirmCustomerEmailAddress_With(wrongConfirmationHash);
        THEN_CustomerEmailAddressConfirmationFailed();
    }

    @Test
    @Order(4)
    void confirmEmailAddress_whenItWasAlreadyConfirmed() {
        GIVEN_CustomerRegistered();
        __and_CustomerEmailAddressWasConfirmed();
        WHEN_ConfirmCustomerEmailAddress_With(confirmationHash);
        THEN_NoEvent();
    }

    @Test
    @Order(5)
    void confirmEmailAddress_withWrongConfirmationHash_whenItWasAlreadyConfirmed() {
        GIVEN_CustomerRegistered();
        __and_CustomerEmailAddressWasConfirmed();
        WHEN_ConfirmCustomerEmailAddress_With(wrongConfirmationHash);
        THEN_CustomerEmailAddressConfirmationFailed();
    }

    @Test
    @Order(6)
    void changeCustomerEmailAddress() {
        // Given
        GIVEN_CustomerRegistered();
        WHEN_ChangeCustomerEmailAddress_With(changedEmailAddress);
        THEN_CustomerEmailAddressChanged();
    }

    @Test
    @Order(7)
    void changeCustomerEmailAddress_withUnchangedEmailAddress() {
        GIVEN_CustomerRegistered();
        WHEN_ChangeCustomerEmailAddress_With(emailAddress);
        THEN_NoEvent();
    }

    @Test
    @Order(8)
    void changeCustomerEmailAddress_whenItWasAlreadyChanged() {
        GIVEN_CustomerRegistered();
        __and_CustomerEmailAddressWasChanged();
        WHEN_ChangeCustomerEmailAddress_With(changedEmailAddress);
        THEN_NoEvent();
    }

    @Test
    @Order(9)
    void confirmCustomerEmailAddress_whenItWasPreviouslyConfirmedAndThenChanged() {
        GIVEN_CustomerRegistered();
        __and_CustomerEmailAddressWasConfirmed();
        __and_CustomerEmailAddressWasChanged();
        WHEN_ConfirmCustomerEmailAddress_With(changedConfirmationHash);
        THEN_CustomerEmailAddressConfirmed();
    }

    /**
     * Methods for GIVEN
     */

    private void GIVEN_CustomerRegistered() {
        registeredCustomer = Customer1.reconstitute(
                List.of(
                        CustomerRegistered.build(customerID, emailAddress, confirmationHash, name)
                )
        );
    }

    private void __and_CustomerEmailAddressWasConfirmed() {
        registeredCustomer.apply(
                CustomerEmailAddressConfirmed.build(customerID)
        );
    }

    private void __and_CustomerEmailAddressWasChanged() {
        registeredCustomer.apply(
                CustomerEmailAddressChanged.build(customerID, changedEmailAddress, changedConfirmationHash)
        );
    }

    /**
     * Methods for WHEN
     */

    private void WHEN_RegisterCustomer() {
        var registerCustomer = RegisterCustomer.build(emailAddress.value, name.givenName, name.familyName);
        registeredCustomer = Customer1.register(registerCustomer);
        customerID = registerCustomer.customerID;
        confirmationHash = registerCustomer.confirmationHash;
    }

    private void WHEN_ConfirmCustomerEmailAddress_With(Hash confirmationHash) {
        var command = ConfirmCustomerEmailAddress.build(customerID.value, confirmationHash.value);
        try {
            registeredCustomer.confirmEmailAddress(command);
        } catch (NullPointerException e) {
            fail(THelper.propertyIsNull("confirmationHash"));
        }
    }

    private void WHEN_ChangeCustomerEmailAddress_With(EmailAddress emailAddress) {
        var command = ChangeCustomerEmailAddress.build(customerID.value, emailAddress.value);
        try {
            registeredCustomer.changeEmailAddress(command);
        } catch (NullPointerException e) {
            fail(THelper.propertyIsNull("emailAddress"));
        }
    }

    /**
     * Methods for THEN
     */

    void THEN_CustomerRegistered() {
        var method = "register";
        var recordedEvents = registeredCustomer.getRecordedEvents();
        assertEquals(1, recordedEvents.size(), THelper.noEventWasRecorded(method, "CustomerRegistered"));
        var event = recordedEvents.get(0);
        assertNotNull(event, THelper.eventIsNull(method));
        assertEquals(CustomerRegistered.class, event.getClass(), THelper.eventOfWrongTypeWasRecorded(method));
        assertEquals(customerID, ((CustomerRegistered) event).customerID, THelper.propertyIsWrong(method, "customerID"));
        assertEquals(emailAddress, ((CustomerRegistered) event).emailAddress, THelper.propertyIsWrong(method, "emailAddress"));
        assertEquals(confirmationHash, ((CustomerRegistered) event).confirmationHash, THelper.propertyIsWrong(method, "confirmationHash"));
        assertEquals(name, ((CustomerRegistered) event).name, THelper.propertyIsWrong(method, "name"));
    }

    void THEN_CustomerEmailAddressConfirmed() {
        var method = "confirmEmailAddress";
        var recordedEvents = registeredCustomer.getRecordedEvents();
        assertEquals(1, recordedEvents.size(), THelper.noEventWasRecorded(method, "CustomerEmailAddressConfirmed"));
        var event = recordedEvents.get(0);
        assertNotNull(event, THelper.eventIsNull(method));
        assertEquals(CustomerEmailAddressConfirmed.class, event.getClass(), THelper.eventOfWrongTypeWasRecorded(method));
        assertEquals(customerID, ((CustomerEmailAddressConfirmed) event).customerID, THelper.propertyIsWrong(method, "customerID"));
    }

    void THEN_CustomerEmailAddressConfirmationFailed() {
        var method = "confirmEmailAddress";
        var recordedEvents = registeredCustomer.getRecordedEvents();
        assertEquals(1, recordedEvents.size(), THelper.noEventWasRecorded(method, "CustomerEmailAddressConfirmationFailed"));
        var event = recordedEvents.get(0);
        assertNotNull(event, THelper.eventIsNull(method));
        assertEquals(CustomerEmailAddressConfirmationFailed.class, event.getClass(), THelper.eventOfWrongTypeWasRecorded(method));
        assertEquals(customerID, ((CustomerEmailAddressConfirmationFailed) event).customerID, THelper.propertyIsWrong(method, "customerID"));
    }

    private void THEN_CustomerEmailAddressChanged() {
        var method = "changeEmailAddress";
        var recordedEvents = registeredCustomer.getRecordedEvents();
        assertEquals(1, recordedEvents.size(), THelper.noEventWasRecorded(method, "CustomerEmailAddressChanged"));
        var event = recordedEvents.get(0);
        assertNotNull(event, THelper.eventIsNull(method));
        assertEquals(CustomerEmailAddressChanged.class, event.getClass(), THelper.eventOfWrongTypeWasRecorded(method));
        assertEquals(customerID, ((CustomerEmailAddressChanged) event).customerID, THelper.propertyIsWrong(method, "customerID"));
        assertEquals(changedEmailAddress, ((CustomerEmailAddressChanged) event).emailAddress, THelper.propertyIsWrong(method, "emailAddress"));
    }

    void THEN_NoEvent() {
        var recordedEvents = registeredCustomer.getRecordedEvents();
        assertEquals(0, registeredCustomer.getRecordedEvents().size(), THelper.noEventShouldHaveBeenRecorded(THelper.typeOfFirst(recordedEvents)));
    }
}
