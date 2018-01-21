class PaymentsController < ApplicationController
  before_action :authenticate_user!
  @@conversion_rate = 214748364.8

  def get_credit
    @user = params.has_key?(:uid) ? User.where(:uid => params[:uid]).take : current_user
    render json: "{ \"credit\": \"" + @user.credit.to_s + "\" }"
  end

  def get_usable_bytes
    @user = params.has_key?(:uid) ? User.where(:uid => params[:uid]).take : current_user
    num_of_bytes = @user.credit * @@conversion_rate
    render json: "{ \"bytes\": \"" + num_of_bytes.to_s + "\" }"
  end

  def execute_transaction
    @user_provider = current_user
    @user_receiver = User.where(:uid => params[:uid]).take
    num_of_bytes = params[:bytes]
    transaction_credit = num_of_bytes.to_f / @@conversion_rate
    user_provider_credit_curr = @user_provider.credit
    user_receiver_credit_curr = @user_receiver.credit
    @user_provider.update!(credit: user_provider_credit_curr + transaction_credit)
    @user_receiver.update!(credit: user_receiver_credit_curr - transaction_credit)

    render json: "{ \"credit\": \"" + @user_provider.credit.to_s + "\" }"
  end

  def client_token
    render json: "{ \"client_token\" : \"" + gateway.client_token.generate + "\"}"
  end

  def checkout
    nonce = params[:payment_method_nonce]
    amount = params[:amount]

    result = gateway.transaction.sale(
      amount: amount,
      payment_method_nonce: nonce,
      :options => {
        :submit_for_settlement => true
      }
    )

    if result.success? || result.transaction
      @user_provider = current_user
      user_provider_credit_curr = @user_provider.credit
      @user_provider.update!(credit: user_provider_credit_curr + amount.to_f)

      render json: "{\"status\":\"success\"}"
    else
      render json: "{ \"errors\": [{\"error\": \"Unable to submit transaction. Try again later.\"}]}"
    end
  end

  protected

  def gateway
    @gateway = Braintree::Gateway.new(
      :environment => :sandbox,
      :merchant_id => "2d743qb2qq2y4xqt",
      :public_key => "4q99v47dj6bpmcb5",
      :private_key => "fd6fdaf302030b612f80f0b5c6dbd782",
    )
  end

end
