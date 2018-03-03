class User < ActiveRecord::Base
  # Include default devise modules.
  devise :database_authenticatable, :registerable,
          :recoverable, :rememberable, :validatable,
          :confirmable
  validates :phone, presence: true, uniqueness: true
  validates :phone, presence: true
  include DeviseTokenAuth::Concerns::User

end
