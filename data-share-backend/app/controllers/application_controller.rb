class ApplicationController < ActionController::API
  def root
    render plain: "ROOT"
  end
end
