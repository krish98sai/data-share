class PaymentsController < ApplicationController
  before_action :authenticate_user!
  def get_credit
    @user = params.has_key?(:uid) ? User.where(:uid => params[:uid]).take : current_user
    render json: @user.credit
  end


end
