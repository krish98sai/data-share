class RegistrationsController < DeviseTokenAuth::RegistrationsController
  def create
    super do |resource|
      resource.credit = 0
      resource.save!
    end
  end
end
