FROM nginx:stable-alpine

# Copy built Angular files
COPY dist/frontend/browser/ /usr/share/nginx/html/

# Copy your custom nginx config
COPY /nginx/conf.d/default.conf /etc/nginx/conf.d/default.conf

EXPOSE 3000

CMD ["nginx", "-g", "daemon off;"]
