require 'test_helper'

class PaymentsControllerTest < ActionDispatch::IntegrationTest
  test "should get get_credit" do
    get payments_get_credit_url
    assert_response :success
  end

end
