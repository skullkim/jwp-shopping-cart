package woowacourse.shoppingcart.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woowacourse.auth.dto.EmailDto;
import woowacourse.auth.dto.TokenResponse;
import woowacourse.auth.support.JwtTokenProvider;
import woowacourse.shoppingcart.application.dto.CustomerDto;
import woowacourse.shoppingcart.application.dto.ModifiedCustomerDto;
import woowacourse.shoppingcart.application.dto.SignInDto;
import woowacourse.shoppingcart.domain.customer.Customer;
import woowacourse.shoppingcart.domain.customer.Email;
import woowacourse.shoppingcart.domain.customer.password.Password;
import woowacourse.shoppingcart.domain.customer.password.PasswordFactory;
import woowacourse.shoppingcart.domain.customer.password.PasswordType;
import woowacourse.shoppingcart.dto.CustomerResponse;
import woowacourse.shoppingcart.repository.CustomerRepository;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final JwtTokenProvider provider;

    public CustomerService(final CustomerRepository customerRepository, final JwtTokenProvider provider) {
        this.customerRepository = customerRepository;
        this.provider = provider;
    }

    public Long createCustomer(final CustomerDto newCustomer) {
        final Customer customer = CustomerDto.toCustomer(newCustomer);
        try {
            return customerRepository.createCustomer(customer);
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("이미 가입한 사용자입니다.");
        }
    }

    public TokenResponse signIn(final SignInDto signInDto) {
        final Email email = new Email(signInDto.getEmail());
        final Password password = PasswordFactory.of(PasswordType.HASHED, signInDto.getPassword());

        final Password foundPassword = customerRepository.findPasswordByEmail(email);
        verifyPassword(password, foundPassword);
        Customer tokenPayloadDto = customerRepository.findByUserEmail(email);
        String payload = createPayload(new EmailDto(email.getValue()));
        return new TokenResponse(tokenPayloadDto.getId(), provider.createToken(payload));
    }

    private void verifyPassword(final Password hashedPassword, final Password existedPassword) {
        if (!hashedPassword.isSamePassword(existedPassword)) {
            throw new IllegalArgumentException("올바르지 않은 비밀번호입니다.");
        }
    }

    private String createPayload(final EmailDto email) {
        try {
            ObjectMapper mapper = new JsonMapper();
            return mapper.writeValueAsString(email);
        } catch (JsonProcessingException e) {
            throw new UnsupportedOperationException(e.getMessage());
        }
    }

    public void updateCustomer(ModifiedCustomerDto modifiedCustomerDto) {
        final Customer modifiedCustomer = ModifiedCustomerDto.toModifiedCustomerDto(modifiedCustomerDto);
        customerRepository.updateCustomer(modifiedCustomer);
    }

    public CustomerResponse findCustomerByEmail(EmailDto email) {
        final Customer customer = customerRepository.findByUserEmail(new Email(email.getEmail()));
        return CustomerResponse.fromCustomer(customer);
    }

    public void deleteCustomer(final EmailDto emailDto) {
        customerRepository.deleteCustomer(new Email(emailDto.getEmail()));
    }
}
