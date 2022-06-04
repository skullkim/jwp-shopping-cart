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
import woowacourse.shoppingcart.dao.CustomerDao;
import woowacourse.shoppingcart.domain.customer.Customer;
import woowacourse.shoppingcart.domain.customer.Email;
import woowacourse.shoppingcart.domain.customer.Password;
import woowacourse.shoppingcart.dto.CustomerResponse;

@Service
@Transactional
public class CustomerService {

    private final CustomerDao customerDao;
    private final JwtTokenProvider provider;

    public CustomerService(CustomerDao customerDao, JwtTokenProvider provider) {
        this.customerDao = customerDao;
        this.provider = provider;
    }

    public Long createCustomer(final CustomerDto newCustomer) {
        final Customer customer = CustomerDto.toCustomer(newCustomer);
        try {
            return customerDao.createCustomer(customer);
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("이미 가입한 사용자입니다.");
        }
    }

    public TokenResponse signIn(final SignInDto signInDto) {
        final Email email = new Email(signInDto.getEmail());
        final Password password = new Password(signInDto.getPassword());
        final String foundPassword = customerDao.findPasswordByEmail(email);
        verifyPassword(password, foundPassword);
        CustomerResponse tokenPayloadDto = customerDao.findByUserEmail(email);
        String payload = createPayload(new EmailDto(email.getValue()));
        return new TokenResponse(tokenPayloadDto.getId(), provider.createToken(payload));
    }

    private void verifyPassword(final Password password, final String hashedPassword) {
        if (!password.isSamePassword(hashedPassword)) {
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
        final int affectedRows = customerDao.updateCustomer(modifiedCustomer);
        if (affectedRows != 1) {
            throw new IllegalArgumentException("업데이트가 되지 않았습니다.");
        }
    }

    public CustomerResponse findCustomerByEmail(EmailDto email) {
        return customerDao.findByUserEmail(new Email(email.getEmail()));
    }

    public void deleteCustomer(final EmailDto emailDto) {
        final int affectedRows = customerDao.deleteCustomer(new Email(emailDto.getEmail()));
        if (affectedRows != 1) {
            throw new IllegalArgumentException("삭제가 되지 않았습니다.");
        }
    }
}
