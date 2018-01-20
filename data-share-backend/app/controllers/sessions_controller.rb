class SessionsController < DeviseTokenAuth::SessionsController
  include DeviseTokenAuth::Concerns::SetUserByToken
  def check_token
    unless authenticate_user!
      render json: "{\"status\": \"success\"}"
    end
  end
end
