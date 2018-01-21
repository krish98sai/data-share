Rails.application.routes.draw do
  get 'payments/get_credit'
  get 'payments/get_usable_bytes'
  get 'payments/client_token'
  post 'payments/execute_transaction'
  post 'payments/checkout'

  root 'application#root'

  mount_devise_token_auth_for 'User', at: 'auth', controllers: {
    sessions: 'sessions',
    registrations: 'registrations'
  }

  devise_scope :user do
    get 'check_token' => 'sessions#check_token'
  end
  # For details on the DSL available within this file, see http://guides.rubyonrails.org/routing.html
end
