Rails.application.routes.draw do
  root 'application#root'

  mount_devise_token_auth_for 'User', at: 'auth', controllers: {
    sessions: 'sessions'
  }

  devise_scope :user do
    get 'check_token' => 'sessions#check_token'
  end
  # For details on the DSL available within this file, see http://guides.rubyonrails.org/routing.html
end
