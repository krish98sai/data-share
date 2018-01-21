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
end
