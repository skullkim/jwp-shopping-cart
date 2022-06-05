package woowacourse.shoppingcart.dao;

import java.util.function.Supplier;
import woowacourse.shoppingcart.application.dto.AddressDto;
import woowacourse.shoppingcart.application.dto.CustomerDto;
import woowacourse.shoppingcart.domain.address.FullAddress;
import woowacourse.shoppingcart.domain.customer.Birthday;
import woowacourse.shoppingcart.domain.customer.Contact;
import woowacourse.shoppingcart.domain.customer.Customer;
import woowacourse.shoppingcart.domain.customer.Email;
import woowacourse.shoppingcart.domain.customer.Gender;
import woowacourse.shoppingcart.domain.customer.Name;
import woowacourse.shoppingcart.domain.customer.Terms;
import woowacourse.shoppingcart.domain.customer.password.PasswordFactory;
import woowacourse.shoppingcart.domain.customer.password.PasswordType;

public class CustomerFixture {

    public static final CustomerDto tommyDto = new CustomerDto("her0807@naver.com", "password1!",
            "example.com", "토미", "male", "1988-08-07",
            "12345678910",
            new AddressDto("a", "b", "12345"), true);

    public static final Customer updatedTommyDto = new Customer(1L, new Email("her0807@naver.com"),
            PasswordFactory.of(PasswordType.HASHED, "password1!"),
            "example.com", new Name("토미"), Gender.MALE, new Birthday("1988-08-07"),
            new Contact("01987654321"),
            new FullAddress("d", "e", "54321"), new Terms(true));

    public static final Supplier<Customer> tommyCreator = () -> new Customer(1L, new Email("her0807@naver.com"),
            PasswordFactory.of(PasswordType.HASHED, "password1!"),
            "example.com", new Name("토미"), Gender.MALE, new Birthday("1988-08-07"),
            new Contact("12345678910"),
            new FullAddress("a", "b", "12345"), new Terms(true));

    public static final Customer tommy = tommyCreator.get();
}
